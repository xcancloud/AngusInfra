package cloud.xcan.angus.cache.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.cache.CachePersistence;
import cloud.xcan.angus.cache.autoconfigure.HybridCacheJpaBootstrapTest.JpaTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:hybrid-cache-ac;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver"
    }
)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(JpaTestApplication.class)
class HybridCacheJpaBootstrapTest {

  @Autowired
  CachePersistence cachePersistence;

  @Test
  void autoConfigurationSelectsSpringAdapterWhenJpaRepositoryAvailable() {
    assertThat(cachePersistence).isInstanceOf(SpringCachePersistenceAdapter.class);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @ImportAutoConfiguration(HybridCacheAutoConfiguration.class)
  static class JpaTestApplication {

  }
}
