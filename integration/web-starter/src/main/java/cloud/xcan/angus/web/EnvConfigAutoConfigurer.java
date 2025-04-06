package cloud.xcan.angus.web;

import cloud.xcan.angus.core.spring.env.DefaultAngusEnvFileLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "xcan.env.enabled", havingValue = "true", matchIfMissing = true)
public class EnvConfigAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean(EnvironmentPostProcessor.class)
  public EnvironmentPostProcessor envFileLoader() {
    return new DefaultAngusEnvFileLoader();
  }

}
