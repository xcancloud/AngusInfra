package cloud.xcan.angus.persistence.jpa.multitenancy;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Optional default {@link AsyncConfigurer} with {@link TenantAwareTaskDecorator}. Disabled when the
 * application defines its own {@link AsyncConfigurer}, or when
 * {@code xcan.jpa.multitenancy.async.enabled=false}.
 */
@AutoConfiguration
@EnableAsync
@ConditionalOnClass(EnableAsync.class)
@ConditionalOnMissingBean(AsyncConfigurer.class)
@ConditionalOnProperty(prefix = "xcan.jpa.multitenancy.async", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TenantAsyncAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(TenantAwareTaskDecorator.class)
  public TenantAwareTaskDecorator tenantAwareTaskDecorator() {
    return new TenantAwareTaskDecorator();
  }

  @Bean
  public AsyncConfigurer tenantAsyncConfigurer(TenantAwareTaskDecorator tenantAwareTaskDecorator) {
    return new AsyncConfigurer() {
      @Override
      public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Math.max(1, Runtime.getRuntime().availableProcessors());
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors * 4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("TenantAwareTaskExecutor-");
        executor.setTaskDecorator(tenantAwareTaskDecorator);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
      }
    };
  }
}
