package cloud.xcan.angus.cache.autoconfigure;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.HybridCacheManager;
import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.entry.CacheEntry;
import cloud.xcan.angus.cache.jpa.SpringCachePersistenceAdapter;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnClass(JpaRepository.class)
public class HybridCacheAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnBean(EntityManagerFactory.class)
  @EntityScan(basePackageClasses = CacheEntry.class)
  @EnableJpaRepositories(basePackageClasses = SpringDataCacheEntryRepository.class)
  static class CacheJpaRepositoryConfiguration {

  }

  /**
   * Chooses persistence at <em>bean creation</em> time via {@link ObjectProvider}, so a
   * {@link SpringDataCacheEntryRepository} registered by JPA is used as the default. Falls back to
   * {@link NoOpCachePersistence} (pure in-memory) when JPA jpa is not available.
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

}
