package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.HybridCacheManager;
import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import cloud.xcan.angus.cache.web.CacheManagementController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class HybridCacheAutoConfiguration {

  /**
   * JPA-backed persistence adapter — registered when a {@link SpringDataCacheEntryRepository}
   * bean is present (i.e. Spring Data JPA is on the classpath and a datasource is configured).
   */
  @Bean
  @ConditionalOnBean(SpringDataCacheEntryRepository.class)
  @ConditionalOnMissingBean(CachePersistence.class)
  public CachePersistence cachePersistence(
      ObjectProvider<SpringDataCacheEntryRepository> repositoryProvider) {
    return new SpringCachePersistenceAdapter(repositoryProvider.getIfAvailable());
  }

  /**
   * Pure in-memory fallback persistence — used when no JPA repository is available.
   * Data is not durable across restarts. Suitable for local/dev environments.
   */
  @Bean
  @ConditionalOnMissingBean(CachePersistence.class)
  public CachePersistence noOpCachePersistence() {
    return new NoOpCachePersistence();
  }

  @Bean
  @ConditionalOnMissingBean(IDistributedCache.class)
  public IDistributedCache distributedCache(CachePersistence persistence,
      CacheProperties cacheProperties) {
    HybridCacheManager core = new HybridCacheManager(persistence, cacheProperties);
    return new TransactionalDistributedCache(core);
  }

  /**
   * Cache management REST controller — opt-in via {@code angus.cache.management.enabled=true}.
   *
   * <p><strong>Security notice:</strong> these endpoints can read, write and clear all cached
   * data. Always protect them with authentication (e.g. Spring Security) before enabling in
   * non-local environments.
   */
  @Bean
  @ConditionalOnProperty(name = "angus.cache.management.enabled", havingValue = "true")
  @ConditionalOnMissingBean(CacheManagementController.class)
  public CacheManagementController cacheManagementController(IDistributedCache cache) {
    return new CacheManagementController(cache);
  }
}
