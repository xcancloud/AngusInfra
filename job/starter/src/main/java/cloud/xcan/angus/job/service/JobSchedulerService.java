package cloud.xcan.angus.job.service;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.entity.JobShard;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.enums.ShardStatus;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.executor.JobExecutorRegistry;
import cloud.xcan.angus.job.executor.MapReduceJobExecutor;
import cloud.xcan.angus.job.executor.ShardingJobExecutor;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.jpa.JobShardRepository;
import cloud.xcan.angus.job.jpa.ScheduledJobRepository;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Core scheduler that polls for due jobs and dispatches them for execution.
 *
 * <h3>Thread-safety design</h3>
 * <ul>
 *   <li>The shared {@code jobExecutorPool} is injected rather than created per
 *       execution — no thread-pool leaks.</li>
 *   <li>Map and sharding phases run lambda bodies with their own {@link TransactionTemplate}
 *       because Spring's {@code @Transactional} does not propagate across thread
 *       boundaries. Each lambda opens, commits, or rolls back its own transaction.</li>
 *   <li>The main {@link #executeJob} method is {@code @Transactional} for the
 *       overall job-status update.</li>
 * </ul>
 *
 * <h3>Distributed mutual exclusion</h3>
 * Each job is protected by a distributed lock acquired via {@link DistributedLockService}
 * before execution begins and released in a {@code finally} block.  Lock acquisition
 * uses the atomic delete-then-insert strategy to avoid TOCTOU races.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobSchedulerService {

  private final ScheduledJobRepository jobRepository;
  private final JobExecutionLogRepository executionLogRepository;
  private final JobShardRepository shardRepository;
  private final DistributedLockService lockService;
  private final JobExecutorRegistry executorRegistry;
  private final JobProperties properties;
  private final ThreadPoolTaskExecutor jobExecutorPool;
  private final PlatformTransactionManager transactionManager;

  /**
   * Stable per-node identifier included in lock ownership and execution-log records.
   */
  private final String nodeId = UUID.randomUUID().toString();

  // ---------------------------------------------------------------------------
  // Scheduler entity point
  // ---------------------------------------------------------------------------

  @Scheduled(fixedDelayString = "${angus.job.scan-interval-ms:1000}")
  public void scanAndExecuteJobs() {
    // Limit results per cycle to prevent OOM / executor-pool saturation when
    // many jobs become overdue simultaneously (e.g. after a maintenance window).
    PageRequest limit = PageRequest.of(0, properties.getMaxJobsPerScan());
    List<ScheduledJob> jobs = jobRepository.findByStatusAndNextExecuteTimeBefore(
        JobStatus.READY, LocalDateTime.now(), limit);
    for (ScheduledJob job : jobs) {
      executeJobWithLock(job);
    }
  }

  // ---------------------------------------------------------------------------
  // Lock + dispatch
  // ---------------------------------------------------------------------------

  private void executeJobWithLock(ScheduledJob job) {
    String lockKey = "job_lock_" + job.getId();
    String lockValue = lockService.tryLock(lockKey, nodeId, properties.getLockTimeoutSeconds());
    if (lockValue == null) {
      log.debug("Job {} is locked by another node", job.getJobName());
      return;
    }
    try {
      executeJob(job);
    } finally {
      lockService.unlock(lockKey, nodeId, lockValue);
    }
  }

  // ---------------------------------------------------------------------------
  // Main execution (outer transaction for status updates)
  // ---------------------------------------------------------------------------

  @Transactional
  public void executeJob(ScheduledJob job) {
    log.info("Executing job: {} (id={})", job.getJobName(), job.getId());

    job.setStatus(JobStatus.RUNNING);
    job.setLastExecuteTime(LocalDateTime.now());
    jobRepository.save(job);

    try {
      switch (job.getJobType()) {
        case SIMPLE -> executeSimpleJob(job);
        case MAP_REDUCE -> executeMapReduceJob(job);
        case SHARDING -> executeShardingJob(job);
      }

      // Update next execution time using Spring's CronExpression (no Quartz needed).
      updateNextExecuteTime(job);
      if (job.getStatus() != JobStatus.FAILED) {
        job.setStatus(JobStatus.READY);
        job.setRetryCount(0);
      }

    } catch (Exception e) {
      log.error("Job execution failed: {}", job.getJobName(), e);
      handleJobFailure(job, e);
    } finally {
      jobRepository.save(job);
    }
  }

  // ---------------------------------------------------------------------------
  // SIMPLE job
  // ---------------------------------------------------------------------------

  private void executeSimpleJob(ScheduledJob job) {
    long startTime = System.currentTimeMillis();
    JobExecutionLog executionLog = initLog(job, null);

    try {
      JobExecutor executor = executorRegistry.getExecutor(job.getBeanName());
      JobExecutionResult result = executor.execute(buildContext(job, null));

      executionLog.setStatus(
          result.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
      executionLog.setResult(result.getResult());
      executionLog.setErrorMessage(result.getErrorMessage());

    } catch (Exception e) {
      executionLog.setStatus(ExecutionStatus.FAILURE);
      executionLog.setErrorMessage(e.getMessage());
      throw e;
    } finally {
      executionLog.setEndTime(LocalDateTime.now());
      executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
      executionLogRepository.save(executionLog);
    }
  }

  // ---------------------------------------------------------------------------
  // MAP_REDUCE job  (P0 fixes: injected pool, TransactionTemplate in lambdas)
  // ---------------------------------------------------------------------------

  private void executeMapReduceJob(ScheduledJob job) {
    log.info("Executing MapReduce job: {}", job.getJobName());
    List<JobShard> shards = createShards(job);
    TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

    // Map phase — each shard runs in the shared pool with its own transaction.
    List<CompletableFuture<String>> futures = shards.stream()
        .map(shard -> CompletableFuture.supplyAsync(
            () -> txTemplate.execute(status -> executeMapPhase(job, shard)),
            jobExecutorPool))
        .toList();

    List<String> mapResults = new ArrayList<>();
    for (CompletableFuture<String> future : futures) {
      try {
        String result = future.get(properties.getLockTimeoutSeconds(), TimeUnit.SECONDS);
        if (result != null) {
          mapResults.add(result);
        }
      } catch (Exception e) {
        log.error("Map phase failed for job {}", job.getJobName(), e);
        throw new RuntimeException("Map phase failed", e);
      }
    }

    executeReducePhase(job, mapResults);
  }

  private String executeMapPhase(ScheduledJob job, JobShard shard) {
    shard.setStatus(ShardStatus.RUNNING);
    shard.setExecutorNode(nodeId);
    shard.setStartTime(LocalDateTime.now());
    shardRepository.save(shard);

    try {
      JobExecutor raw = executorRegistry.getExecutor(job.getBeanName());
      if (!(raw instanceof MapReduceJobExecutor executor)) {
        throw new IllegalArgumentException(
            "Executor '" + job.getBeanName() + "' does not implement MapReduceJobExecutor "
                + "(required for MAP_REDUCE job type). Actual type: " + raw.getClass().getName());
      }
      List<String> mapResult = executor.map(
          buildContext(job, shard.getShardingItem()),
          shard.getShardingItem(),
          shard.getShardingParameter());

      String result = String.join(",", mapResult);
      shard.setMapResult(result);
      shard.setStatus(ShardStatus.COMPLETED);
      return result;

    } catch (Exception e) {
      shard.setStatus(ShardStatus.FAILED);
      throw e;
    } finally {
      shard.setEndTime(LocalDateTime.now());
      shardRepository.save(shard);
    }
  }

  private void executeReducePhase(ScheduledJob job, List<String> mapResults) {
    long startTime = System.currentTimeMillis();
    // Sentinel value -1 indicates the reduce phase in execution logs.
    JobExecutionLog executionLog = initLog(job, -1);

    try {
      JobExecutor raw = executorRegistry.getExecutor(job.getBeanName());
      if (!(raw instanceof MapReduceJobExecutor executor)) {
        throw new IllegalArgumentException(
            "Executor '" + job.getBeanName() + "' does not implement MapReduceJobExecutor "
                + "(required for MAP_REDUCE reduce phase). Actual type: " + raw.getClass()
                .getName());
      }
      String result = executor.reduce(buildContext(job, null), mapResults);
      executionLog.setStatus(ExecutionStatus.SUCCESS);
      executionLog.setResult(result);

    } catch (Exception e) {
      executionLog.setStatus(ExecutionStatus.FAILURE);
      executionLog.setErrorMessage(e.getMessage());
      throw e;
    } finally {
      executionLog.setEndTime(LocalDateTime.now());
      executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
      executionLogRepository.save(executionLog);
    }
  }

  // ---------------------------------------------------------------------------
  // SHARDING job  (P0 fixes: injected pool, TransactionTemplate in lambdas)
  // ---------------------------------------------------------------------------

  private void executeShardingJob(ScheduledJob job) {
    List<JobShard> shards = createShards(job);
    TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

    List<CompletableFuture<Void>> futures = shards.stream()
        .map(shard -> CompletableFuture.runAsync(
            () -> txTemplate.executeWithoutResult(status -> executeShardingItem(job, shard)),
            jobExecutorPool))
        .toList();

    boolean anyFailed = false;
    for (CompletableFuture<Void> future : futures) {
      try {
        future.get(properties.getLockTimeoutSeconds(), TimeUnit.SECONDS);
      } catch (Exception e) {
        log.error("Sharding execution failed for job {}", job.getJobName(), e);
        anyFailed = true;
      }
    }

    if (anyFailed) {
      throw new RuntimeException("One or more shards failed for job: " + job.getJobName());
    }
  }

  private void executeShardingItem(ScheduledJob job, JobShard shard) {
    long startTime = System.currentTimeMillis();
    JobExecutionLog executionLog = initLog(job, shard.getShardingItem());

    shard.setStatus(ShardStatus.RUNNING);
    shard.setExecutorNode(nodeId);
    shard.setStartTime(LocalDateTime.now());
    shardRepository.save(shard);

    try {
      JobExecutor raw = executorRegistry.getExecutor(job.getBeanName());
      if (!(raw instanceof ShardingJobExecutor executor)) {
        throw new IllegalArgumentException(
            "Executor '" + job.getBeanName() + "' does not implement ShardingJobExecutor "
                + "(required for SHARDING job type). Actual type: " + raw.getClass().getName());
      }
      JobExecutionResult result = executor.executeSharding(
          buildContext(job, shard.getShardingItem()),
          shard.getShardingItem(),
          shard.getShardingParameter());

      shard.setStatus(result.isSuccess() ? ShardStatus.COMPLETED : ShardStatus.FAILED);
      executionLog.setStatus(
          result.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
      executionLog.setResult(result.getResult());
      executionLog.setErrorMessage(result.getErrorMessage());

    } catch (Exception e) {
      shard.setStatus(ShardStatus.FAILED);
      executionLog.setStatus(ExecutionStatus.FAILURE);
      executionLog.setErrorMessage(e.getMessage());
      throw e;
    } finally {
      shard.setEndTime(LocalDateTime.now());
      shardRepository.save(shard);
      executionLog.setEndTime(LocalDateTime.now());
      executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
      executionLogRepository.save(executionLog);
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Creates a fresh set of shards for this execution, replacing any leftovers from previous runs.
   *
   * <p><strong>Transaction isolation fix:</strong> this method runs in its own
   * {@code REQUIRES_NEW} transaction so that the inserted shard rows are
   * <em>committed</em> before the child-thread {@link TransactionTemplate}s
   * start.  Without this, child threads (which use separate DB connections with READ_COMMITTED
   * isolation) would not see the parent's uncommitted inserts and would fail with "entity not
   * found" or issue duplicate inserts.
   */
  private List<JobShard> createShards(ScheduledJob job) {
    TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
    requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return requiresNew.execute(status -> {
      shardRepository.deleteByJobId(job.getId());

      String[] parameters = (job.getShardingParameter() != null)
          ? job.getShardingParameter().split(",")
          : new String[0];

      int count = job.getShardingCount() != null ? job.getShardingCount() : 1;
      List<JobShard> shards = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        JobShard shard = new JobShard();
        shard.setJobId(job.getId());
        shard.setShardingItem(i);
        shard.setShardingParameter(i < parameters.length ? parameters[i].trim() : "");
        shard.setStatus(ShardStatus.PENDING);
        shards.add(shard);
      }
      return shardRepository.saveAll(shards);
    });
  }

  private JobContext buildContext(ScheduledJob job, Integer shardingItem) {
    return JobContext.builder()
        .jobId(job.getId())
        .jobName(job.getJobName())
        .jobGroup(job.getJobGroup())
        .jobType(job.getJobType())
        .shardingItem(shardingItem)
        .totalShardingCount(job.getShardingCount())
        .executeTime(LocalDateTime.now())
        .build();
  }

  private JobExecutionLog initLog(ScheduledJob job, Integer shardingItem) {
    JobExecutionLog log = new JobExecutionLog();
    log.setJobId(job.getId());
    log.setJobName(job.getJobName());
    log.setShardingItem(shardingItem);
    log.setStatus(ExecutionStatus.RUNNING);
    log.setStartTime(LocalDateTime.now());
    log.setExecutorNode(nodeId);
    return log;
  }

  /**
   * Advances the job's {@code nextExecuteTime} based on its cron expression.
   *
   * <p>If the cron expression cannot be parsed, the job is set to FAILED to
   * prevent an infinite re-scheduling loop (P0 fix).
   */
  private void updateNextExecuteTime(ScheduledJob job) {
    try {
      CronExpression cron = CronExpression.parse(job.getCronExpression());
      LocalDateTime next = cron.next(LocalDateTime.now());
      if (next == null) {
        log.error("Cron expression '{}' has no future fire times for job '{}'; marking FAILED.",
            job.getCronExpression(), job.getJobName());
        job.setStatus(JobStatus.FAILED);
        return;
      }
      job.setNextExecuteTime(next);
    } catch (Exception e) {
      log.error("Failed to parse cron expression '{}' for job '{}'; marking FAILED.",
          job.getCronExpression(), job.getJobName(), e);
      // Mark the job FAILED so it is not picked up again (P0 fix — prevents infinite loop).
      job.setStatus(JobStatus.FAILED);
    }
  }

  private void handleJobFailure(ScheduledJob job, Exception e) {
    int retryCount = job.getRetryCount() != null ? job.getRetryCount() : 0;
    int maxRetry = job.getMaxRetryCount() != null ? job.getMaxRetryCount() : 0;
    job.setRetryCount(retryCount + 1);

    if (retryCount + 1 >= maxRetry) {
      job.setStatus(JobStatus.FAILED);
      log.error("Job {} failed after {} retry attempt(s); status set to FAILED.",
          job.getJobName(), maxRetry);
    } else {
      job.setStatus(JobStatus.READY);
      job.setNextExecuteTime(LocalDateTime.now().plusMinutes(properties.getRetryBackoffMinutes()));
      log.warn("Job {} will be retried (attempt {}/{})", job.getJobName(), retryCount + 1,
          maxRetry);
    }
  }
}
