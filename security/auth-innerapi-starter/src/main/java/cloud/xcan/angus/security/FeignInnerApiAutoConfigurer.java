package cloud.xcan.angus.security;

import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;


@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "xcan.auth.innerapi", name = "enabled", matchIfMissing = true)
public class FeignInnerApiAutoConfigurer {

  @Bean
  public FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor(
      ClientSignInnerApiRemote clientSignInnerApiRemote,
      ConfigurableEnvironment configurableEnvironment) {
    return new FeignInnerApiAuthInterceptor(clientSignInnerApiRemote, configurableEnvironment);
  }

}
