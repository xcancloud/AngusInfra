package cloud.xcan.angus.web;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.spring.scheduled.SchedulerPropertis;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * By default Spring Boot will use just a single thread for all scheduled tasks to run on. This is
 * not ideal, because these tasks will be blocking. Instead we will configure the scheduler to run
 * each scheduled tasks on a separate thread (if there is enough threads available).
 *
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({SchedulerPropertis.class})
public class SchedulerAutoConfigurer implements SchedulingConfigurer {

  @Resource
  private SchedulerPropertis propertis;

  /**
   * Configures the scheduler to allow multiple pools.
   *
   * @param taskRegistrar The task registrar.
   */
  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(propertis.getThreadPoolSize());
    threadPoolTaskScheduler.setThreadNamePrefix(
        new Str0(new long[]{0x6788F19143F55F35L, 0x47FD4ECB7A0921ABL, 0xAE1A4EA6C2D34405L})
            .toString() /* => "SDF-Scheduler" */);
    threadPoolTaskScheduler.initialize();
    taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
  }

}
