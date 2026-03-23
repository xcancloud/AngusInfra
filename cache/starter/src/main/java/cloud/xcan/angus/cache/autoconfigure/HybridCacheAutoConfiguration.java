package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.HybridCacheManager;
import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import cloud.xcan.angus.cache.web.CacheManagementController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class HybridCacheAutoConfiguration {

  /**
   * Chooses persistence at <em>bean creation</em> time via {@link ObjectProvider}, so a
   * {@link SpringDataCacheEntryRepository} registered by JPA (possibly after this configuration
   * class is parsed) is still visible. Using {@code @ConditionalOnBean} on a separate method can
   * miss the repository when this configuration is {@code @Import}ed next to
   * {@code @EnableJpaRepositories}.
   */
  @Bean
  @ConditionalOnMissingBean(CachePersistence.class)
  public CachePersistence cachePersistence(
      ObjectProvider<SpringDataCacheEntryRepository> repositoryProvider) {
    SpringDataCacheEntryRepository repository = repositoryProvider.getIfAvailable();
    return repository != null
        ? new SpringCachePersistenceAdapter(repository)
        : new NoOpCachePersistence();
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
