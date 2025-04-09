package cloud.xcan.angus.security;

import cloud.xcan.angus.core.spring.condition.PrivateEditionCondition;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@Conditional(PrivateEditionCondition.class)
@ConditionalOnProperty(prefix = "xcan.auth.openapi2p", name = "enabled", matchIfMissing = false)
public class FeignOpenapi2pAutoConfigurer {

  @Bean
  public FeignOpenapi2pAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignOpenapi2pRemote clientSign2pOpenRemote) {
    return new FeignOpenapi2pAuthInterceptor(clientSign2pOpenRemote);
  }

}
