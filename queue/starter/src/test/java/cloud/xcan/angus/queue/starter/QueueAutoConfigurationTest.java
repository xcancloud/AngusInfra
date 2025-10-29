package cloud.xcan.angus.queue.starter;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.queue.core.service.AuditLogger;
import cloud.xcan.angus.queue.core.service.QueueAdminService;
import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.starter.scheduler.DeadLetterMoverScheduler;
import cloud.xcan.angus.queue.starter.scheduler.DlqSoftDeletePurgerScheduler;
import cloud.xcan.angus.queue.starter.scheduler.LeaseReaperScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class QueueAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(
          DataSourceAutoConfiguration.class,
          HibernateJpaAutoConfiguration.class,
          QueueAutoConfiguration.class
      ))
      .withPropertyValues(
          // H2 in-memory datasource for JPA bootstrapping
          "spring.datasource.url=jdbc:h2:mem:queue-test;DB_CLOSE_DELAY=-1",
          "spring.datasource.driverClassName=org.h2.Driver",
          "spring.datasource.username=sa",
          "spring.datasource.password=",
          // Disable DDL actions to avoid schema validation
          "spring.jpa.hibernate.ddl-auto=none",
          "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
          // Queue properties
          "angus.queue.scheduling.enabled=true",
          "angus.queue.partitions=4",
          "angus.queue.reclaimBatch=77",
          "angus.queue.deadLetterMoveBatch=66"
      );

  @Test
  void defaultBeansPresent() {
    contextRunner.run(ctx -> {
      assertThat(ctx).hasSingleBean(RepositoryAdapter.class);
      assertThat(ctx).hasSingleBean(QueueService.class);
      assertThat(ctx).hasSingleBean(QueueAdminService.class);
      assertThat(ctx).hasSingleBean(AuditLogger.class);
      assertThat(ctx).hasSingleBean(LeaseReaperScheduler.class);
      assertThat(ctx).hasSingleBean(DeadLetterMoverScheduler.class);
      assertThat(ctx).hasSingleBean(DlqSoftDeletePurgerScheduler.class);
    });
  }

  @Test
  void softDeleteBeanOverridesWhenEnabled() {
    contextRunner
        .withPropertyValues("angus.queue.admin.soft-delete-dlq=true")
        .run(ctx -> {
          assertThat(ctx).hasSingleBean(QueueAdminService.class);
          assertThat(ctx.getBean(QueueAdminService.class)).isNotNull();
        });
  }
}
