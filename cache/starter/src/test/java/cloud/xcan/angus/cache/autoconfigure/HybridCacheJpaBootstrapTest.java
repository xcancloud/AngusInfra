package cloud.xcan.angus.cache.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.entry.CacheEntry;
import cloud.xcan.angus.cache.jpa.SpringDataCacheEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest(
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:hybrid-cache-ac;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver"
    }
)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@EntityScan(basePackageClasses = CacheEntry.class)
@EnableJpaRepositories(basePackageClasses = SpringDataCacheEntryRepository.class)
@Import(HybridCacheAutoConfiguration.class)
class HybridCacheJpaBootstrapTest {

  @Autowired
  CachePersistence cachePersistence;

  @Test
  void autoConfigurationSelectsSpringAdapterWhenJpaRepositoryAvailable() {
    assertThat(cachePersistence).isInstanceOf(SpringCachePersistenceAdapter.class);
  }
}
