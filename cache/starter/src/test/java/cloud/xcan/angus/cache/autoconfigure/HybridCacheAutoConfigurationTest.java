package cloud.xcan.angus.cache.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

class HybridCacheAutoConfigurationTest {

  @Configuration
  @EnableTransactionManagement
  static class TxDataSourceConfig {

    @Bean
    DataSource dataSource() {
      return new EmbeddedDatabaseBuilder()
          .setType(EmbeddedDatabaseType.H2)
          .build();
    }

    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }

  private final ApplicationContextRunner baseRunner = new ApplicationContextRunner()
      .withUserConfiguration(TxDataSourceConfig.class)
      .withConfiguration(AutoConfigurations.of(HybridCacheAutoConfiguration.class));

  @Test
  void noJpaRepository_registersNoOpPersistenceAndTransactionalCache() {
    baseRunner.run(ctx -> {
      assertThat(ctx).hasSingleBean(IDistributedCache.class);
      assertThat(ctx).hasSingleBean(CachePersistence.class);
      assertThat(ctx.getBean(CachePersistence.class)).isInstanceOf(NoOpCachePersistence.class);
      Object cacheTarget =
          AopTestUtils.getUltimateTargetObject(ctx.getBean(IDistributedCache.class));
      assertThat(cacheTarget).isInstanceOf(TransactionalDistributedCache.class);
    });
  }

  @Test
  void customDistributedCacheOverridesDefault() {
    baseRunner
        .withUserConfiguration(CustomCacheConfig.class)
        .run(ctx -> assertThat(ctx.getBean(IDistributedCache.class))
            .isInstanceOf(StubDistributedCache.class));
  }

  @Configuration
  static class CustomCacheConfig {

    @Bean
    IDistributedCache customDistributedCache() {
      return new StubDistributedCache();
    }
  }

  static final class StubDistributedCache implements IDistributedCache {

    @Override
    public void set(String key, String value, Long ttlSeconds) {
    }

    @Override
    public void set(String key, String value) {
    }

    @Override
    public Optional<String> get(String key) {
      return Optional.empty();
    }

    @Override
    public boolean delete(String key) {
      return false;
    }

    @Override
    public boolean exists(String key) {
      return false;
    }

    @Override
    public long getTTL(String key) {
      return -2;
    }

    @Override
    public boolean expire(String key, long ttlSeconds) {
      return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public CacheStats getStats() {
      return CacheStats.builder().build();
    }

    @Override
    public int cleanupExpiredEntries() {
      return 0;
    }
  }
}
