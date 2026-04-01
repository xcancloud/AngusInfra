package cloud.xcan.angus.security;

import cloud.xcan.angus.core.spring.condition.PrivateEditionCondition;
import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.security.model.cache.CacheType;
import cloud.xcan.angus.security.model.cache.DistributedTokenStore;
import cloud.xcan.angus.security.model.cache.LocalTokenStore;
import cloud.xcan.angus.security.model.cache.TokenStore;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring Boot Auto-Configuration for OpenAPI 2P Authentication
 *
 * <p>Creates the appropriate {@link TokenStore} bean based on the configured cache type.</p>
 *
 * @author Framework Team
 * @version 3.0 (TokenStore abstraction)
 * @since 2025-03-22
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Openapi2pAuthProperties.class)
@Conditional(PrivateEditionCondition.class)
@ConditionalOnProperty(prefix = "angus.auth.openapi2p", name = "enabled", matchIfMissing = false)
public class FeignOpenapi2pAutoConfigurer {

  @Bean
  public TokenStore openapi2pTokenStore(
      Openapi2pAuthProperties properties,
      ObjectProvider<Object> distributedCacheProvider) {

    if (properties.getCacheType() == CacheType.DISTRIBUTED) {
      Object distributedCache = findDistributedCache(distributedCacheProvider);
      if (distributedCache != null) {
        log.info("Using distributed token store for OpenAPI 2P authentication");
        return new DistributedTokenStore(distributedCache);
      }
      log.warn("Distributed cache requested but no IDistributedCache bean found. "
          + "Falling back to local token store.");
    }

    log.info("Using local (in-memory) token store for OpenAPI 2P authentication");
    return new LocalTokenStore();
  }

  @Bean
  public FeignOpenapi2pAuthInterceptor feignOpen2pAuthInterceptor(
      ClientSignOpenapi2pRemote clientSign2pOpenRemote,
      Openapi2pAuthProperties properties,
      ConfigurableEnvironment environment,
      TokenStore openapi2pTokenStore) {
    return new FeignOpenapi2pAuthInterceptor(
        clientSign2pOpenRemote, properties, environment, openapi2pTokenStore);
  }

  private Object findDistributedCache(ObjectProvider<Object> provider) {
    try {
      Class<?> cacheInterface = Class.forName("cloud.xcan.angus.cache.IDistributedCache");
      return provider.stream()
          .filter(bean -> cacheInterface.isAssignableFrom(bean.getClass()))
          .findFirst()
          .orElse(null);
    } catch (ClassNotFoundException e) {
      log.debug("IDistributedCache class not found on classpath");
      return null;
    }
  }
}
