package cloud.xcan.angus.job.config;

import cloud.xcan.angus.job.properties.JobProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Spring Boot auto-configuration for the Job scheduler module.
 *
 * <p>Registers the shared thread pools used by
 * {@link cloud.xcan.angus.job.service.JobSchedulerService}. All thread counts are driven by
 * {@link JobProperties} instead of being hard-coded.
 *
 * <p>The {@code @EnableScheduling} annotation activates Spring's scheduling
 * infrastructure so that {@code @Scheduled} methods on
 * {@link cloud.xcan.angus.job.monitor.JobHealthMonitor} and
 * {@link cloud.xcan.angus.job.service.JobSchedulerService} are picked up.
 *
 * <p>JPA repositories and entity scanning are handled by Spring Boot's
 * auto-configuration ({@code JpaRepositoriesAutoConfiguration}) — no hard-coded package strings are
 * needed here.
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(JobProperties.class)
public class JobAutoConfiguration {

  /**
   * Shared executor pool for job and shard work items. Used by
   * {@link cloud.xcan.angus.job.service.JobSchedulerService} for SHARDING and MAP_REDUCE parallel
   * phases.  Injected by name {@code "jobExecutorPool"}.
   */
  @Bean(name = "jobExecutorPool")
  @ConditionalOnMissingBean(name = "jobExecutorPool")
  public ThreadPoolTaskExecutor jobExecutorPool(JobProperties props) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(props.getExecutorCorePoolSize());
    executor.setMaxPoolSize(props.getExecutorMaxPoolSize());
    executor.setQueueCapacity(props.getExecutorQueueCapacity());
    executor.setThreadNamePrefix("job-executor-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  /**
   * Scheduler pool for Spring's {@code @Scheduled} task dispatcher.
   */
  @Bean(name = "taskScheduler")
  @ConditionalOnMissingBean(name = "taskScheduler")
  public ThreadPoolTaskScheduler taskScheduler(JobProperties props) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(props.getSchedulerPoolSize());
    scheduler.setThreadNamePrefix("job-scheduler-");
    scheduler.initialize();
    return scheduler;
  }
}
