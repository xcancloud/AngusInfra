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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class HybridCacheAutoConfiguration {

  @Bean
  @ConditionalOnBean(SpringDataCacheEntryRepository.class)
  @ConditionalOnMissingBean(CachePersistence.class)
  public CachePersistence cachePersistence(
      ObjectProvider<SpringDataCacheEntryRepository> repositoryProvider) {
    SpringDataCacheEntryRepository repository = repositoryProvider.getIfAvailable();
    return new SpringCachePersistenceAdapter(repository);
  }

  @Bean
  @ConditionalOnMissingBean(IDistributedCache.class)
  public IDistributedCache distributedCache(CachePersistence persistence, CacheProperties cacheProperties) {
    // Core implementation with configuration
    HybridCacheManager core = new HybridCacheManager(persistence, cacheProperties);
    // Wrap with transactional proxy
    return new TransactionalDistributedCache(core);
  }

  @Bean
  @ConditionalOnMissingBean(CacheManagementController.class)
  public CacheManagementController cacheManagementController(IDistributedCache cache) {
    return new CacheManagementController(cache);
  }
}
