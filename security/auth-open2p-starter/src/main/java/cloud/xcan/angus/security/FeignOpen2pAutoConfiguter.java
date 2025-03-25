package cloud.xcan.angus.security;

import cloud.xcan.angus.security.remote.ClientSignOpen2pRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "xcan.open2pauth", name = "enabled", matchIfMissing = false)
public class FeignOpen2pAutoConfiguter {

  @Bean
  public FeignOpen2pAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignOpen2pRemote clientSign2pOpenRemote) {
    return new FeignOpen2pAuthInterceptor(clientSign2pOpenRemote);
  }

}
