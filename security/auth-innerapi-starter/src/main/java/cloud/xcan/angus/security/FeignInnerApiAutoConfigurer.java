package cloud.xcan.angus.security;

import cloud.xcan.angus.security.cache.TokenCacheManager;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import cloud.xcan.angus.security.model.cache.CacheType;
import cloud.xcan.angus.security.model.cache.DistributedTokenStore;
import cloud.xcan.angus.security.model.cache.LocalTokenStore;
import cloud.xcan.angus.security.model.cache.TokenStore;
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Auto-Configuration for Inner API Authentication
 *
 * <p>Creates the appropriate {@link TokenStore} bean based on the configured cache type:</p>
 * <ul>
 *   <li>{@code local} (default): In-memory volatile-based cache for single-instance</li>
 *   <li>{@code distributed}: IDistributedCache-backed store for multi-instance</li>
 * </ul>
 *
 * @author Framework Team
 * @version 3.0 (TokenStore abstraction)
 * @since 2025-03-22
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InnerApiAuthProperties.class)
@ConditionalOnProperty(
    prefix = "xcan.auth.innerapi",
    name = "enabled",
    matchIfMissing = true
)
public class FeignInnerApiAutoConfigurer {

  @Bean
  public TokenStore innerApiTokenStore(
      InnerApiAuthProperties properties,
      ObjectProvider<Object> distributedCacheProvider) {

    if (properties.getCacheType() == CacheType.DISTRIBUTED) {
      Object distributedCache = findDistributedCache(distributedCacheProvider);
      if (distributedCache != null) {
        log.info("Using distributed token store for inner API authentication");
        return new DistributedTokenStore(distributedCache);
      }
      log.warn("Distributed cache requested but no IDistributedCache bean found. "
          + "Falling back to local token store.");
    }

    log.info("Using local (in-memory) token store for inner API authentication");
    return new LocalTokenStore();
  }

  @Bean
  public TokenCacheManager tokenCacheManager(
      InnerApiAuthProperties properties,
      ClientSignInnerApiRemote clientSignInnerApiRemote,
      TokenStore innerApiTokenStore) {
    log.info("Creating TokenCacheManager bean with {} store",
        properties.getCacheType());
    return new TokenCacheManager(properties, clientSignInnerApiRemote, innerApiTokenStore);
  }

  @Bean
  public FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor(
      TokenCacheManager tokenCacheManager,
      InnerApiAuthProperties properties) {
    log.info("Creating FeignInnerApiAuthInterceptor bean");
    return new FeignInnerApiAuthInterceptor(tokenCacheManager, properties);
  }

  /**
   * Attempt to find an IDistributedCache bean from the application context.
   * Uses class name matching to avoid hard dependency on cache module.
   */
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
