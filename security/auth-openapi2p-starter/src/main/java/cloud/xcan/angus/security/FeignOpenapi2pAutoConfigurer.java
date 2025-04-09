package cloud.xcan.angus.security;

import cloud.xcan.angus.security.model.remote.ClientSignOpenapi2pRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "xcan.open2pauth", name = "enabled", matchIfMissing = false)
public class FeignOpenapi2pAutoConfigurer {

  @Bean
  public FeignOpenapi2pAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignOpenapi2pRemote clientSign2pOpenRemote) {
    return new FeignOpenapi2pAuthInterceptor(clientSign2pOpenRemote);
  }

}
