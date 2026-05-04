package cloud.xcan.angus.queue.autoconfigure;

import cloud.xcan.angus.queue.adapter.JpaRepositoryAdapter;
import cloud.xcan.angus.queue.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.entity.MessageEntity;
import cloud.xcan.angus.queue.jpa.DeadLetterRepository;
import cloud.xcan.angus.queue.jpa.MessageRepository;
import cloud.xcan.angus.queue.scheduler.DeadLetterMoverScheduler;
import cloud.xcan.angus.queue.service.AuditLogger;
import cloud.xcan.angus.queue.service.DefaultQueueAdminService;
import cloud.xcan.angus.queue.service.DefaultQueueService;
import cloud.xcan.angus.queue.service.QueueAdminService;
import cloud.xcan.angus.queue.service.QueueService;
import cloud.xcan.angus.queue.service.Slf4jAuditLogger;
import cloud.xcan.angus.queue.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.scheduler.DlqSoftDeletePurgerScheduler;
import cloud.xcan.angus.queue.scheduler.LeaseReaperScheduler;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties(QueueProperties.class)
@ConditionalOnClass(JpaRepository.class)
public class QueueAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnBean(EntityManagerFactory.class)
  @EntityScan(basePackageClasses = {MessageEntity.class, DeadLetterEntity.class})
  @EnableJpaRepositories(basePackageClasses = {MessageRepository.class, DeadLetterRepository.class})
  static class QueueJpaRepositoryConfiguration {

  }

  @Bean
  @ConditionalOnMissingBean(RepositoryAdapter.class)
  public RepositoryAdapter jpaRepositoryAdapter(MessageRepository messageRepository,
      DeadLetterRepository deadLetterRepository) {
    return new JpaRepositoryAdapter(messageRepository, deadLetterRepository);
  }

  @Bean
  @ConditionalOnMissingBean(QueueService.class)
  public QueueService queueService(RepositoryAdapter adapter) {
    return new DefaultQueueService(adapter);
  }

  @Bean
  public TaskScheduler queueTaskScheduler(QueueProperties props) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(props.getScheduling().getPoolSize());
    scheduler.setThreadNamePrefix(props.getScheduling().getThreadNamePrefix());
    scheduler.setRemoveOnCancelPolicy(true);
    scheduler.initialize();
    return scheduler;
  }

  @Bean
  @ConditionalOnProperty(prefix = "angus.queue.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
  public LeaseReaperScheduler leaseReaper(QueueAdminService adminService,
      QueueProperties properties) {
    return new LeaseReaperScheduler(adminService, properties);
  }

  @Bean
  @ConditionalOnProperty(prefix = "angus.queue.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
  public DeadLetterMoverScheduler deadLetterMover(QueueService queueService,
      QueueProperties properties) {
    return new DeadLetterMoverScheduler(queueService, properties);
  }

  @Bean
  @ConditionalOnMissingBean(AuditLogger.class)
  public AuditLogger auditLogger() {
    return new Slf4jAuditLogger();
  }

  @Bean
  @ConditionalOnMissingBean(QueueAdminService.class)
  @ConditionalOnProperty(prefix = "angus.queue.admin", name = "soft-delete-dlq", havingValue = "false", matchIfMissing = true)
  public QueueAdminService queueAdminService(RepositoryAdapter adapter) {
    return new DefaultQueueAdminService(adapter);
  }

  @Bean
  @ConditionalOnMissingBean(QueueAdminService.class)
  @ConditionalOnProperty(prefix = "angus.queue.admin", name = "soft-delete-dlq", havingValue = "true")
  public QueueAdminService softDeleteQueueAdminService(RepositoryAdapter adapter,
      AuditLogger auditLogger) {
    return new DefaultQueueAdminService(adapter, auditLogger, true);
  }

  @Bean
  @ConditionalOnProperty(prefix = "angus.queue.admin", name = "soft-delete-dlq", havingValue = "true")
  public DlqSoftDeletePurgerScheduler dlqSoftDeletePurgerScheduler(
      DeadLetterRepository deadLetterRepository, QueueProperties properties) {
    return new DlqSoftDeletePurgerScheduler(deadLetterRepository, properties);
  }
}
