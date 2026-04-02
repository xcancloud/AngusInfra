package cloud.xcan.angus.job.autoconfigure;

import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.jpa.ScheduledJobRepository;
import cloud.xcan.angus.job.properties.JobProperties;
import cloud.xcan.angus.job.registrar.JobRegistrar;
import cloud.xcan.angus.job.service.JobManagementService;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
 * <p>JPA: {@link JobJpaBootstrapConfiguration} registers {@link EntityScan} for job entities and
 * {@link EnableJpaRepositories} for {@code cloud.xcan.angus.job.jpa} repositories when an
 * {@link EntityManagerFactory} is present (runs after {@link HibernateJpaAutoConfiguration}).
 *
 * <p>Also registers {@link JobRegistrar} which scans all {@link JobExecutor} beans annotated with
 * {@link cloud.xcan.angus.job.annotation.JobDefinition} and auto-registers them into the
 * {@code scheduled_job} table on startup (idempotent).
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties(JobProperties.class)
public class JobAutoConfiguration {

  /**
   * Binds job module entities and Spring Data repositories without relying on the application's
   * {@code @SpringBootApplication} scan base.
   */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(EntityManagerFactory.class)
  @ConditionalOnBean(EntityManagerFactory.class)
  @EntityScan(basePackageClasses = ScheduledJob.class)
  @EnableJpaRepositories(basePackageClasses = ScheduledJobRepository.class)
  static class JobJpaBootstrapConfiguration {

  }

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

  /**
   * Registers all {@link JobExecutor} beans annotated with
   * {@link cloud.xcan.angus.job.annotation.JobDefinition} into {@code scheduled_job} on startup.
   *
   * <p>The bean is conditional so that applications can provide their own {@link JobRegistrar}
   * sub-class if custom registration logic is required.
   */
  @Bean
  @ConditionalOnBean(JobManagementService.class)
  @ConditionalOnMissingBean(JobRegistrar.class)
  public JobRegistrar jobRegistrar(
      JobManagementService jobManagementService,
      ScheduledJobRepository jobRepository,
      Map<String, JobExecutor> jobExecutors) {
    return new JobRegistrar(jobManagementService, jobRepository, jobExecutors);
  }
}
