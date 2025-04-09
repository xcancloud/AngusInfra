package cloud.xcan.angus.security;

import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "xcan.auth.innerapi", name = "enabled", matchIfMissing = true)
public class FeignInnerApiAutoConfigurer {

  @Bean
  public FeignInnerApiAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignInnerApiRemote clientSignInnerApiRemote) {
    return new FeignInnerApiAuthInterceptor(clientSignInnerApiRemote);
  }

}
