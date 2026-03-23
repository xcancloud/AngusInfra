package cloud.xcan.angus.job.config;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.job.properties.JobProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class JobAutoConfigurationTest {

  @Test
  void registersExecutorAndSchedulerBeans() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JobAutoConfiguration.class))
        .withPropertyValues(
            "angus.job.executor-core-pool-size=2",
            "angus.job.executor-max-pool-size=4",
            "angus.job.executor-queue-capacity=20",
            "angus.job.scheduler-pool-size=3"
        )
        .run(ctx -> {
          assertThat(ctx).hasSingleBean(JobProperties.class);
          assertThat(ctx.getBean("jobExecutorPool", ThreadPoolTaskExecutor.class).getCorePoolSize())
              .isEqualTo(2);
          assertThat(ctx.getBean("taskScheduler", ThreadPoolTaskScheduler.class)
              .getScheduledThreadPoolExecutor()
              .getCorePoolSize())
              .isEqualTo(3);
        });
  }
}
