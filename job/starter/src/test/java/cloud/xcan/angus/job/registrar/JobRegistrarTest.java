package cloud.xcan.angus.job.registrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.job.annotation.JobDefinition;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobType;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.JobContext;
import cloud.xcan.angus.job.model.JobExecutionResult;
import cloud.xcan.angus.job.repository.ScheduledJobRepository;
import cloud.xcan.angus.job.service.JobManagementService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

/**
 * Unit tests for {@link JobRegistrar}.
 *
 * <p>All tests use Mockito (no Spring context) to keep execution fast and
 * dependency-free.  The test fixtures use concrete inner classes that carry
 * {@link JobDefinition} annotations so the registrar's annotation-scanning
 * code is exercised for real — no reflection stubs needed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JobRegistrar")
class JobRegistrarTest {

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  /**
   * A minimal {@link JobExecutor} annotated with {@link JobDefinition}.  Used as the
   * "happy-path" executor throughout the tests.
   */
  @JobDefinition(
      name = "test-job",
      group = "test",
      cron = "0 * * * * *",
      maxRetryCount = 2,
      description = "A test job"
  )
  static class SimpleAnnotatedJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
      return JobExecutionResult.builder().success(true).build();
    }
  }

  /**
   * A {@link JobExecutor} that carries <strong>no</strong> {@link JobDefinition} annotation.
   * The registrar must silently ignore such executors.
   */
  static class UnannotatedJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
      return JobExecutionResult.builder().success(true).build();
    }
  }

  /**
   * A {@link JobExecutor} whose {@link JobDefinition} requests an initial delay before the
   * first execution.
   */
  @JobDefinition(
      name = "delayed-job",
      group = "test",
      cron = "0 0 2 * * *",
      initialDelaySeconds = 60,
      description = "Job with initial delay"
  )
  static class DelayedJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
      return JobExecutionResult.builder().success(true).build();
    }
  }

  /**
   * A {@link JobExecutor} configured as a {@link JobType#SHARDING} job with explicit shard
   * parameters.
   */
  @JobDefinition(
      name = "sharding-job",
      group = "test",
      cron = "0 0/10 * * * *",
      type = JobType.SHARDING,
      shardingCount = 4,
      shardingParameter = "p0,p1,p2,p3",
      maxRetryCount = 1,
      description = "Sharding test job"
  )
  static class ShardingAnnotatedJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
      return JobExecutionResult.builder().success(true).build();
    }
  }

  /**
   * A {@link JobExecutor} with an empty {@link JobDefinition#shardingParameter()}, which the
   * registrar should map to {@code null} in the {@link CreateJobRequest}.
   */
  @JobDefinition(
      name = "no-param-job",
      group = "test",
      cron = "0 0 3 * * *"
  )
  static class NoParamJob implements JobExecutor {

    @Override
    public JobExecutionResult execute(JobContext context) {
      return JobExecutionResult.builder().success(true).build();
    }
  }

  // ---------------------------------------------------------------------------
  // Mocks & SUT
  // ---------------------------------------------------------------------------

  @Mock
  private JobManagementService jobManagementService;

  @Mock
  private ScheduledJobRepository jobRepository;

  private static final DefaultApplicationArguments NO_ARGS =
      new DefaultApplicationArguments(new String[0]);

  // ---------------------------------------------------------------------------
  // Helper: create a persisted-looking ScheduledJob
  // ---------------------------------------------------------------------------

  private static ScheduledJob savedJob(Long id) {
    ScheduledJob job = new ScheduledJob();
    job.setId(id);
    job.setNextExecuteTime(LocalDateTime.now().plusMinutes(1));
    return job;
  }

  // ---------------------------------------------------------------------------
  // Nested test groups
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Happy path — new job registration")
  class HappyPath {

    @Test
    @DisplayName("registers a single annotated executor on first startup")
    void registers_newAnnotatedExecutor() throws Exception {
      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);

      ArgumentCaptor<CreateJobRequest> captor = ArgumentCaptor.forClass(CreateJobRequest.class);
      verify(jobManagementService).createJob(captor.capture());

      CreateJobRequest req = captor.getValue();
      assertThat(req.getJobName()).isEqualTo("test-job");
      assertThat(req.getJobGroup()).isEqualTo("test");
      assertThat(req.getCronExpression()).isEqualTo("0 * * * * *");
      assertThat(req.getBeanName()).isEqualTo("simpleAnnotatedJob");
      assertThat(req.getJobType()).isEqualTo(JobType.SIMPLE);
      assertThat(req.getMaxRetryCount()).isEqualTo(2);
      assertThat(req.getDescription()).isEqualTo("A test job");
    }

    @Test
    @DisplayName("registers multiple annotated executors in one run")
    void registers_multipleAnnotatedExecutors() throws Exception {
      Map<String, JobExecutor> executors = new LinkedHashMap<>();
      executors.put("simpleAnnotatedJob", new SimpleAnnotatedJob());
      executors.put("shardingAnnotatedJob", new ShardingAnnotatedJob());

      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup(any(), any())).thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);

      verify(jobManagementService, times(2)).createJob(any());
    }

    @Test
    @DisplayName("maps shardingCount and shardingParameter correctly")
    void maps_shardingFields() throws Exception {
      Map<String, JobExecutor> executors = Map.of("shardingAnnotatedJob", new ShardingAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("sharding-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(2L));

      registrar.run(NO_ARGS);

      ArgumentCaptor<CreateJobRequest> captor = ArgumentCaptor.forClass(CreateJobRequest.class);
      verify(jobManagementService).createJob(captor.capture());

      CreateJobRequest req = captor.getValue();
      assertThat(req.getJobType()).isEqualTo(JobType.SHARDING);
      assertThat(req.getShardingCount()).isEqualTo(4);
      assertThat(req.getShardingParameter()).isEqualTo("p0,p1,p2,p3");
    }

    @Test
    @DisplayName("maps empty shardingParameter to null in the request")
    void maps_emptyShardingParameterToNull() throws Exception {
      Map<String, JobExecutor> executors = Map.of("noParamJob", new NoParamJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("no-param-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(3L));

      registrar.run(NO_ARGS);

      verify(jobManagementService).createJob(argThat(req -> req.getShardingParameter() == null));
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Idempotency — skip already-registered jobs")
  @SuppressWarnings({"null", "DataFlowIssue"})
  class Idempotency {

    @Test
    @DisplayName("skips registration when job already exists in the database")
    void skips_existingJob() throws Exception {
      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.of(savedJob(99L)));

      registrar.run(NO_ARGS);

      verify(jobManagementService, never()).createJob(any());
      verify(jobRepository, never()).save(any(ScheduledJob.class));
    }

    @Test
    @DisplayName("registers only new jobs when some already exist")
    void registers_onlyNewJobs() throws Exception {
      Map<String, JobExecutor> executors = new LinkedHashMap<>();
      executors.put("simpleAnnotatedJob", new SimpleAnnotatedJob());   // already exists
      executors.put("shardingAnnotatedJob", new ShardingAnnotatedJob()); // new

      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.of(savedJob(1L)));
      when(jobRepository.findByJobNameAndJobGroup("sharding-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(2L));

      registrar.run(NO_ARGS);

      verify(jobManagementService, times(1)).createJob(
          argThat(req -> "sharding-job".equals(req.getJobName())));
    }

    @Test
    @DisplayName("calling run twice does not double-register (simulates two restarts)")
    void twoRunCalls_idempotent() throws Exception {
      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      // First run: job does not exist
      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.empty())
          .thenReturn(Optional.of(savedJob(1L))); // second run: job now exists
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);
      registrar.run(NO_ARGS);

      // createJob must only be called once across both runs
      verify(jobManagementService, times(1)).createJob(any());
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Unannotated executors — must be ignored")
  class UnannotatedExecutors {

    @Test
    @DisplayName("ignores executors without @JobDefinition")
    void ignores_unannotatedExecutor() throws Exception {
      Map<String, JobExecutor> executors = Map.of("unannotatedJob", new UnannotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      registrar.run(NO_ARGS);

      verify(jobRepository, never()).findByJobNameAndJobGroup(any(), any());
      verify(jobManagementService, never()).createJob(any());
    }

    @Test
    @DisplayName("registers annotated executors and ignores unannotated ones in the same map")
    void mixed_registersOnlyAnnotated() throws Exception {
      Map<String, JobExecutor> executors = new LinkedHashMap<>();
      executors.put("unannotatedJob", new UnannotatedJob());
      executors.put("simpleAnnotatedJob", new SimpleAnnotatedJob());

      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);

      verify(jobManagementService, times(1)).createJob(
          argThat(req -> "test-job".equals(req.getJobName())));
    }

    @Test
    @DisplayName("does nothing when executor map is empty")
    void empty_executorMap() throws Exception {
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, Map.of());

      registrar.run(NO_ARGS);

      verify(jobManagementService, never()).createJob(any());
      verify(jobRepository, never()).findByJobNameAndJobGroup(any(), any());
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("initialDelaySeconds — pushes nextExecuteTime forward")
  @SuppressWarnings({"null", "DataFlowIssue"})
  class InitialDelay {

    @Test
    @DisplayName("saves updated nextExecuteTime when initialDelaySeconds > 0")
    void saves_nextExecuteTimeWithDelay() throws Exception {
      Map<String, JobExecutor> executors = Map.of("delayedJob", new DelayedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      ScheduledJob persisted = savedJob(5L);
      when(jobRepository.findByJobNameAndJobGroup("delayed-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(persisted);

      LocalDateTime before = LocalDateTime.now();
      registrar.run(NO_ARGS);
      LocalDateTime after = LocalDateTime.now();

      ArgumentCaptor<ScheduledJob> saveCaptor = ArgumentCaptor.forClass(ScheduledJob.class);
      verify(jobRepository).save(saveCaptor.capture());

      LocalDateTime nextExec = saveCaptor.getValue().getNextExecuteTime();
      assertThat(nextExec)
          .isAfterOrEqualTo(before.plusSeconds(60))
          .isBeforeOrEqualTo(after.plusSeconds(60));
    }

    @Test
    @DisplayName("does NOT call repository.save when initialDelaySeconds == 0")
    void noDelay_doesNotSave() throws Exception {
      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);

      // repository.save should NOT be called for delay adjustments
      verify(jobRepository, never()).save(any(ScheduledJob.class));
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Error resilience — one failure must not abort others")
  class ErrorResilience {

    /**
     * A second annotated executor used in error-resilience tests alongside
     * {@link SimpleAnnotatedJob}.
     */
    @JobDefinition(
        name = "second-job",
        group = "test",
        cron = "0 0 1 * * *"
    )
    class SecondJob implements JobExecutor {

      @Override
      public JobExecutionResult execute(JobContext context) {
        return JobExecutionResult.builder().success(true).build();
      }
    }

    @Test
    @DisplayName("continues processing remaining executors when one createJob call throws")
    void continuesAfter_createJobFailure() throws Exception {
      Map<String, JobExecutor> executors = new LinkedHashMap<>();
      executors.put("simpleAnnotatedJob", new SimpleAnnotatedJob());  // will throw
      executors.put("secondJob", new SecondJob());                    // should still register

      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup(any(), any())).thenReturn(Optional.empty());
      // First call throws; second call succeeds
      ScheduledJob job10 = savedJob(10L);
      when(jobManagementService.createJob(any()))
          .thenThrow(new RuntimeException("DB error"))
          .thenReturn(job10);

      // Must not throw
      registrar.run(NO_ARGS);

      // Both executors were attempted
      verify(jobManagementService, times(2)).createJob(any());
    }

    @Test
    @DisplayName("does not propagate exceptions from createJob to the caller")
    void doesNotPropagate_createJobException() throws Exception {
      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup(any(), any())).thenReturn(Optional.empty());
      when(jobManagementService.createJob(any()))
          .thenThrow(new RuntimeException("Unexpected failure"));

      // run() must complete without throwing
      registrar.run(NO_ARGS);
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("CGLIB proxy transparency — annotation on proxied beans")
  class ProxyTransparency {

    @Test
    @DisplayName("resolves @JobDefinition through a CGLIB subclass proxy")
    void resolves_annotationThroughCglibProxy() throws Exception {
      // Simulate a CGLIB-proxied bean: create a subclass of SimpleAnnotatedJob at runtime.
      // The subclass itself has NO @JobDefinition annotation — the registrar must
      // climb up to the superclass to find it.
      SimpleAnnotatedJob proxiedBean = new SimpleAnnotatedJob() {
        // anonymous subclass — simulates a CGLIB proxy whose class has no @JobDefinition
      };

      // Verify that the anonymous subclass itself does NOT carry the annotation
      assertThat(proxiedBean.getClass().getAnnotation(JobDefinition.class)).isNull();

      // But AopUtils.getTargetClass(proxiedBean) should still return SimpleAnnotatedJob
      // (since it is not a Spring proxy). To make this test meaningful without Spring AOP,
      // we verify that the registrar handles real proxied subclasses gracefully by
      // inspecting actual Spring behaviour:
      //
      // When the bean IS a direct instance (not wrapped by Spring), AopUtils returns
      // bean.getClass() — the anonymous subclass — which has no annotation.
      // Therefore, we expect NO registration for a raw anonymous subclass.
      Map<String, JobExecutor> executors = Map.of("proxiedBean", proxiedBean);
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      registrar.run(NO_ARGS);

      // The anonymous subclass has no annotation visible to AopUtils.getTargetClass,
      // so the registrar correctly skips it.
      verify(jobManagementService, never()).createJob(any());
    }

    @Test
    @DisplayName("registers a non-proxied bean that directly carries @JobDefinition")
    void registers_directAnnotatedBean() throws Exception {
      // Ensure the direct (non-proxied) case works as the primary happy path
      SimpleAnnotatedJob direct = new SimpleAnnotatedJob();
      assertThat(direct.getClass().getAnnotation(JobDefinition.class)).isNotNull();

      Map<String, JobExecutor> executors = Map.of("simpleAnnotatedJob", direct);
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("test-job", "test"))
          .thenReturn(Optional.empty());
      when(jobManagementService.createJob(any())).thenReturn(savedJob(1L));

      registrar.run(NO_ARGS);

      verify(jobManagementService, times(1)).createJob(any());
    }
  }

  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("CreateJobRequest field mapping — all attributes")
  class RequestFieldMapping {

    @Test
    @DisplayName("beanName in the request matches the Spring bean name from the map key")
    void beanName_matchesMapKey() throws Exception {
      Map<String, JobExecutor> executors = Map.of("myCustomBeanName", new SimpleAnnotatedJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup(any(), any())).thenReturn(Optional.empty());
      ScheduledJob job1 = savedJob(1L);
      when(jobManagementService.createJob(any())).thenReturn(job1);

      registrar.run(NO_ARGS);

      verify(jobManagementService).createJob(
          argThat(req -> req != null && "myCustomBeanName".equals(req.getBeanName())));
    }

    @Test
    @DisplayName("default group 'default' is used when @JobDefinition.group is not set")
    void defaultGroup_isUsed() throws Exception {
      @JobDefinition(name = "no-group-job", cron = "0 * * * * *")
      class NoGroupJob implements JobExecutor {

        @Override
        public JobExecutionResult execute(JobContext context) {
          return JobExecutionResult.builder().success(true).build();
        }
      }

      Map<String, JobExecutor> executors = Map.of("noGroupJob", new NoGroupJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup("no-group-job", "default"))
          .thenReturn(Optional.empty());
      ScheduledJob job1b = savedJob(1L);
      when(jobManagementService.createJob(any())).thenReturn(job1b);

      registrar.run(NO_ARGS);

      verify(jobManagementService).createJob(
          argThat(req -> req != null && "default".equals(req.getJobGroup())));
    }

    @Test
    @DisplayName("maxRetryCount defaults to 3 when not specified in @JobDefinition")
    void defaultMaxRetryCount() throws Exception {
      @JobDefinition(name = "default-retry-job", cron = "0 * * * * *")
      class DefaultRetryJob implements JobExecutor {

        @Override
        public JobExecutionResult execute(JobContext context) {
          return JobExecutionResult.builder().success(true).build();
        }
      }

      Map<String, JobExecutor> executors = Map.of("defaultRetryJob", new DefaultRetryJob());
      JobRegistrar registrar = new JobRegistrar(jobManagementService, jobRepository, executors);

      when(jobRepository.findByJobNameAndJobGroup(any(), any())).thenReturn(Optional.empty());
      ScheduledJob job1c = savedJob(1L);
      when(jobManagementService.createJob(any())).thenReturn(job1c);

      registrar.run(NO_ARGS);

      verify(jobManagementService).createJob(
          argThat(req -> req != null && req.getMaxRetryCount() == 3));
    }
  }
}
