package cloud.xcan.angus.security;

import cloud.xcan.angus.core.spring.condition.PrivateEditionCondition;
import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot Auto-Configuration for OpenAPI 2P Authentication
 *
 * Enables the OpenAPI 2P Feign interceptor with externalized configuration properties.
 * Activated when xcan.auth.openapi2p.enabled=true and PrivateEdition conditions are met.
 *
 * @author Framework Team
 * @version 2.0
 * @since 2025-03-21
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Openapi2pAuthProperties.class)
@Conditional(PrivateEditionCondition.class)
@ConditionalOnProperty(prefix = "xcan.auth.openapi2p", name = "enabled", matchIfMissing = false)
public class FeignOpenapi2pAutoConfigurer {

  /**
   * Creates the OpenAPI 2P Feign interceptor bean with configuration properties
   * and environment access for credential resolution.
   *
   * @param clientSign2pOpenRemote Feign client for token endpoint
   * @param properties             configuration properties for OpenAPI 2P
   * @param environment            Spring environment for reading credentials
   * @return configured interceptor instance
   */
  @Bean
  public FeignOpenapi2pAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignOpenapi2pRemote clientSign2pOpenRemote,
      Openapi2pAuthProperties properties,
      ConfigurableEnvironment environment) {
    return new FeignOpenapi2pAuthInterceptor(clientSign2pOpenRemote, properties, environment);
  }

}
