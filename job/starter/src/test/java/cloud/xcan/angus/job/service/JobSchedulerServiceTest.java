package cloud.xcan.angus.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.entity.JobShard;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.enums.JobType;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.executor.JobExecutorRegistry;
import cloud.xcan.angus.job.executor.MapReduceJobExecutor;
import cloud.xcan.angus.job.executor.ShardingJobExecutor;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.jpa.JobShardRepository;
import cloud.xcan.angus.job.jpa.ScheduledJobRepository;
import cloud.xcan.angus.job.model.JobExecutionResult;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class JobSchedulerServiceTest {

  @Mock
  private ScheduledJobRepository jobRepository;
  @Mock
  private JobExecutionLogRepository executionLogRepository;
  @Mock
  private JobShardRepository shardRepository;
  @Mock
  private DistributedLockService lockService;
  @Mock
  private JobExecutorRegistry executorRegistry;
  @Mock
  private JobProperties properties;

  private ThreadPoolTaskExecutor jobExecutorPool;
  private PlatformTransactionManager transactionManager;
  private JobSchedulerService scheduler;

  @BeforeEach
  void setUp() {
    jobExecutorPool = new ThreadPoolTaskExecutor();
    jobExecutorPool.setCorePoolSize(2);
    jobExecutorPool.setMaxPoolSize(4);
    jobExecutorPool.setQueueCapacity(100);
    jobExecutorPool.setThreadNamePrefix("test-job-exec-");
    jobExecutorPool.initialize();
    transactionManager = new NoOpTransactionManager();
    lenient().when(properties.getLockTimeoutSeconds()).thenReturn(60);
    lenient().when(properties.getRetryBackoffMinutes()).thenReturn(5);
    lenient().when(properties.getMaxJobsPerScan()).thenReturn(100);
    scheduler = new JobSchedulerService(
        jobRepository,
        executionLogRepository,
        shardRepository,
        lockService,
        executorRegistry,
        properties,
        jobExecutorPool,
        transactionManager
    );
    lenient().when(jobRepository.save(any(ScheduledJob.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    lenient().when(executionLogRepository.save(any(JobExecutionLog.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    lenient().when(shardRepository.save(any(JobShard.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @AfterEach
  void tearDown() {
    jobExecutorPool.shutdown();
  }

  @Test
  @DisplayName("scanAndExecuteJobs skips when lock not acquired")
  void scan_skipsWhenLockContended() {
    ScheduledJob job = readyJob(1L, JobType.SIMPLE);
    when(jobRepository.findByStatusAndNextExecuteTimeBefore(
        eq(JobStatus.READY), any(LocalDateTime.class), any()))
        .thenReturn(List.of(job));
    when(lockService.tryLock(anyString(), anyString(), anyInt())).thenReturn(null);

    scheduler.scanAndExecuteJobs();

    verify(lockService).tryLock(anyString(), anyString(), anyInt());
    verify(executorRegistry, never()).getExecutor(anyString());
  }

  @Test
  @DisplayName("executeJob SIMPLE success leaves job READY")
  void simple_success() {
    ScheduledJob job = readyJob(10L, JobType.SIMPLE);
    JobExecutor exec = mock(JobExecutor.class);
    when(exec.execute(any())).thenReturn(
        JobExecutionResult.builder().success(true).result("ok").build());
    when(executorRegistry.getExecutor("exec")).thenReturn(exec);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
    assertThat(job.getRetryCount()).isEqualTo(0);
    assertThat(job.getNextExecuteTime()).isNotNull();
    verify(executionLogRepository, atLeastOnce()).save(any(JobExecutionLog.class));
  }

  @Test
  @DisplayName("executeJob SIMPLE failure schedules retry when under max")
  void simple_failure_retries() {
    ScheduledJob job = readyJob(11L, JobType.SIMPLE);
    job.setRetryCount(0);
    job.setMaxRetryCount(3);
    JobExecutor exec = mock(JobExecutor.class);
    when(exec.execute(any())).thenThrow(new RuntimeException("fail"));
    when(executorRegistry.getExecutor("exec")).thenReturn(exec);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
    assertThat(job.getRetryCount()).isEqualTo(1);
    assertThat(job.getNextExecuteTime()).isAfter(LocalDateTime.now().minusMinutes(1));
  }

  @Test
  @DisplayName("executeJob SIMPLE failure marks FAILED after max retries")
  void simple_failure_exhausted() {
    ScheduledJob job = readyJob(12L, JobType.SIMPLE);
    job.setRetryCount(2);
    job.setMaxRetryCount(3);
    JobExecutor exec = mock(JobExecutor.class);
    when(exec.execute(any())).thenThrow(new RuntimeException("fail"));
    when(executorRegistry.getExecutor("exec")).thenReturn(exec);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
    assertThat(job.getRetryCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("executeJob marks FAILED when cron cannot be advanced")
  void simple_invalidCronAfterRun() {
    ScheduledJob job = readyJob(13L, JobType.SIMPLE);
    job.setCronExpression("not a cron at all !!!");
    JobExecutor exec = mock(JobExecutor.class);
    when(exec.execute(any())).thenReturn(
        JobExecutionResult.builder().success(true).build());
    when(executorRegistry.getExecutor("exec")).thenReturn(exec);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
  }

  @Test
  @DisplayName("executeJob MAP_REDUCE runs map and reduce")
  void mapReduce_success() {
    ScheduledJob job = readyJob(20L, JobType.MAP_REDUCE);
    job.setShardingCount(2);
    job.setShardingParameter("a,b");
    stubSaveAllShardsWithIds();

    MapReduceJobExecutor mr = mock(MapReduceJobExecutor.class);
    when(mr.map(any(), anyInt(), anyString())).thenReturn(List.of("x"));
    when(mr.reduce(any(), anyList())).thenReturn("merged");
    when(executorRegistry.getExecutor("exec")).thenReturn(mr);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
    verify(mr, atLeastOnce()).map(any(), anyInt(), anyString());
    verify(mr).reduce(any(), anyList());
    ArgumentCaptor<JobExecutionLog> logCap = ArgumentCaptor.forClass(JobExecutionLog.class);
    verify(executionLogRepository, atLeastOnce()).save(logCap.capture());
    assertThat(logCap.getAllValues()).anyMatch(
        l -> l.getShardingItem() != null && l.getShardingItem() == -1
            && ExecutionStatus.SUCCESS.equals(l.getStatus()));
  }

  @Test
  @DisplayName("executeJob MAP_REDUCE fails when executor is wrong type")
  void mapReduce_wrongExecutorType() {
    ScheduledJob job = readyJob(21L, JobType.MAP_REDUCE);
    job.setShardingCount(1);
    stubSaveAllShardsWithIds();
    when(executorRegistry.getExecutor("exec")).thenReturn(mock(JobExecutor.class));

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
    assertThat(job.getRetryCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("executeJob SHARDING aggregates shard futures")
  void sharding_success() {
    ScheduledJob job = readyJob(30L, JobType.SHARDING);
    job.setShardingCount(2);
    job.setShardingParameter("p1,p2");
    stubSaveAllShardsWithIds();

    ShardingJobExecutor se = mock(ShardingJobExecutor.class);
    when(se.executeSharding(any(), anyInt(), anyString())).thenReturn(
        JobExecutionResult.builder().success(true).result("ok").build());
    when(executorRegistry.getExecutor("exec")).thenReturn(se);

    scheduler.executeJob(job);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
    verify(se, atLeastOnce()).executeSharding(any(), anyInt(), anyString());
  }

  @Test
  @DisplayName("executeJob SHARDING fails when any shard throws")
  void sharding_shardFailure() {
    ScheduledJob job = readyJob(31L, JobType.SHARDING);
    job.setShardingCount(1);
    stubSaveAllShardsWithIds();

    ShardingJobExecutor se = mock(ShardingJobExecutor.class);
    when(se.executeSharding(any(), anyInt(), anyString()))
        .thenThrow(new RuntimeException("shard down"));
    when(executorRegistry.getExecutor("exec")).thenReturn(se);

    scheduler.executeJob(job);

    assertThat(job.getRetryCount()).isGreaterThanOrEqualTo(1);
  }

  private void stubSaveAllShardsWithIds() {
    when(shardRepository.saveAll(anyList())).thenAnswer(inv -> {
      @SuppressWarnings("unchecked")
      List<JobShard> shards = new ArrayList<>((List<JobShard>) inv.getArgument(0));
      long id = 100;
      for (JobShard s : shards) {
        s.setId(id++);
      }
      return shards;
    });
  }

  private static ScheduledJob readyJob(Long id, JobType type) {
    ScheduledJob job = new ScheduledJob();
    job.setId(id);
    job.setJobName("job-" + id);
    job.setJobGroup("g");
    job.setStatus(JobStatus.READY);
    job.setCronExpression("0 * * * * *");
    job.setBeanName("exec");
    job.setJobType(type);
    job.setRetryCount(0);
    job.setMaxRetryCount(3);
    job.setShardingCount(type == JobType.SIMPLE ? 1 : 2);
    job.setNextExecuteTime(LocalDateTime.now().minusMinutes(1));
    return job;
  }

  private static final class NoOpTransactionManager implements PlatformTransactionManager {

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition)
        throws TransactionException {
      return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
    }
  }
}
