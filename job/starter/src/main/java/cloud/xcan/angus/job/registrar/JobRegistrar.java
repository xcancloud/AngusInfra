package cloud.xcan.angus.job.registrar;

import cloud.xcan.angus.job.annotation.JobDefinition;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.repository.ScheduledJobRepository;
import cloud.xcan.angus.job.service.JobManagementService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * Scans all {@link JobExecutor} beans annotated with {@link JobDefinition} and registers them into
 * the {@code scheduled_job} table on every application startup.
 *
 * <h3>Idempotency guarantee</h3>
 * <p>If a job with the same {@code name + group} already exists the registration is silently
 * skipped, so that:
 * <ul>
 *   <li>Restarts never overwrite operator changes made via the REST API (e.g. a custom cron).</li>
 *   <li>The same application can be deployed multiple times without duplicate records.</li>
 * </ul>
 *
 * <h3>Ordering</h3>
 * <p>Runs at {@link Order @Order(10)} to execute after standard infrastructure runners
 * (datasource, JPA, etc.) are ready.
 *
 * <h3>Conditional beans</h3>
 * <p>Executors guarded by {@code @Conditional} (e.g. private-edition-only jobs) are simply absent
 * from the {@code jobExecutors} map when their condition evaluates to {@code false}, so no special
 * handling is required here.
 */
@Slf4j
@Order(10)
@RequiredArgsConstructor
public class JobRegistrar implements ApplicationRunner {

  private final JobManagementService jobManagementService;
  private final ScheduledJobRepository jobRepository;

  /**
   * All {@link JobExecutor} beans present in the application context, keyed by Spring bean name.
   * Spring auto-collects them via the {@code Map<String, JobExecutor>} injection point.
   */
  private final Map<String, JobExecutor> jobExecutors;

  @Override
  public void run(ApplicationArguments args) {
    int registered = 0;
    int skipped = 0;
    int failed = 0;

    for (Map.Entry<String, JobExecutor> entry : jobExecutors.entrySet()) {
      String beanName = entry.getKey();
      JobExecutor executor = entry.getValue();

      // Unwrap CGLIB / JDK proxy to reach the real class annotation
      Class<?> targetClass = resolveTargetClass(executor);
      JobDefinition def = targetClass.getAnnotation(JobDefinition.class);
      if (def == null) {
        continue;
      }

      Optional<ScheduledJob> existing =
          jobRepository.findByJobNameAndJobGroup(def.name(), def.group());

      if (existing.isPresent()) {
        log.debug("Job already registered, skipping: name='{}', group='{}'",
            def.name(), def.group());
        skipped++;
        continue;
      }

      try {
        CreateJobRequest req = buildRequest(def, beanName);
        ScheduledJob job = jobManagementService.createJob(req);

        if (def.initialDelaySeconds() > 0) {
          job.setNextExecuteTime(LocalDateTime.now().plusSeconds(def.initialDelaySeconds()));
          jobRepository.save(job);
        }

        log.info("Registered job: name='{}', group='{}', cron='{}', bean='{}'",
            def.name(), def.group(), def.cron(), beanName);
        registered++;

      } catch (Exception e) {
        log.error("Failed to register job: name='{}', group='{}', bean='{}'",
            def.name(), def.group(), beanName, e);
        failed++;
      }
    }

    log.info("Job registration complete — registered: {}, skipped (already exist): {}, failed: {}",
        registered, skipped, failed);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private CreateJobRequest buildRequest(JobDefinition def, String beanName) {
    CreateJobRequest req = new CreateJobRequest();
    req.setJobName(def.name());
    req.setJobGroup(def.group());
    req.setCronExpression(def.cron());
    req.setBeanName(beanName);
    req.setJobType(def.type());
    req.setShardingCount(def.shardingCount());
    req.setShardingParameter(def.shardingParameter().isEmpty() ? null : def.shardingParameter());
    req.setMaxRetryCount(def.maxRetryCount());
    req.setDescription(def.description());
    return req;
  }

  /**
   * Resolves the real target class from a potentially proxied Spring bean.
   *
   * <p>Spring AOP (CGLIB / JDK dynamic proxy) wraps beans in proxy objects at runtime. Annotations
   * placed on the original class are not visible on the proxy class itself, so we need to unwrap
   * one level to reach the user-defined class.
   */
  private static Class<?> resolveTargetClass(Object bean) {
    try {
      // Spring AOP utility — works for both CGLIB and interface-based proxies.
      return org.springframework.aop.support.AopUtils.getTargetClass(bean);
    } catch (Exception e) {
      return bean.getClass();
    }
  }
}
