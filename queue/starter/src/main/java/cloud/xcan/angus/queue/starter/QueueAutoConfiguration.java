package cloud.xcan.angus.queue.starter;

import cloud.xcan.angus.queue.core.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.core.entity.MessageEntity;
import cloud.xcan.angus.queue.core.service.AuditLogger;
import cloud.xcan.angus.queue.core.service.DefaultQueueAdminService;
import cloud.xcan.angus.queue.core.service.DefaultQueueService;
import cloud.xcan.angus.queue.core.service.QueueAdminService;
import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.core.service.Slf4jAuditLogger;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.starter.adapter.JpaRepositoryAdapter;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import cloud.xcan.angus.queue.starter.repository.DeadLetterRepository;
import cloud.xcan.angus.queue.starter.repository.MessageRepository;
import cloud.xcan.angus.queue.starter.scheduler.DeadLetterMoverScheduler;
import cloud.xcan.angus.queue.starter.scheduler.DlqSoftDeletePurgerScheduler;
import cloud.xcan.angus.queue.starter.scheduler.LeaseReaperScheduler;
import cloud.xcan.angus.queue.starter.service.AdminService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(QueueProperties.class)
@EntityScan(basePackageClasses = {MessageEntity.class, DeadLetterEntity.class})
@EnableJpaRepositories(basePackageClasses = {MessageRepository.class, DeadLetterRepository.class})
@ConditionalOnClass(JpaRepository.class)
public class QueueAutoConfiguration {

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
  public AdminService adminService(MessageRepository messageRepository,
      DeadLetterRepository deadLetterRepository) {
    return new AdminService(messageRepository, deadLetterRepository);
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
  @ConditionalOnProperty(prefix = "angus.queue.admin", name = "soft-delete-dlq", havingValue = "true")
  public QueueAdminService softDeleteQueueAdminService(RepositoryAdapter adapter,
      AuditLogger auditLogger) {
    return new DefaultQueueAdminService(adapter, auditLogger, true);
  }

  @Bean
  @ConditionalOnProperty(prefix = "angus.queue.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
  public DlqSoftDeletePurgerScheduler dlqSoftDeletePurgerScheduler(
      DeadLetterRepository deadLetterRepository, QueueProperties properties) {
    return new DlqSoftDeletePurgerScheduler(deadLetterRepository, properties);
  }
}
