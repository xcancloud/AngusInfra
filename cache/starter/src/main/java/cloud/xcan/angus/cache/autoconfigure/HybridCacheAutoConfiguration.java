package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.HybridCacheManager;
import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import cloud.xcan.angus.cache.management.CacheManagementController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
  public IDistributedCache distributedCache(CachePersistence persistence) {
    // Core implementation
    HybridCacheManager core = new HybridCacheManager(persistence);
    // Wrap with transactional proxy
    return new TransactionalDistributedCache(core);
  }

  @Bean
  @ConditionalOnMissingBean(CacheManagementController.class)
  public CacheManagementController cacheManagementController(IDistributedCache cache) {
    return new CacheManagementController(cache);
  }
}
