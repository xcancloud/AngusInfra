package cloud.xcan.angus.plugin.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.plugin.entity.PluginEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaPluginStoreTest.TestJpaApplication.class)
class JpaPluginStoreTest {

  @Autowired
  private PluginRepository repository;

  private JpaPluginStore store;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    store = new JpaPluginStore(repository);
  }

  @Test
  void listStoreGetDelete() throws Exception {
    assertTrue(store.listPluginIds().isEmpty());

    Path stored = store.storePlugin("jpa.p", new byte[]{1, 2, 3});
    assertNotNull(stored);
    assertTrue(Files.exists(stored));

    List<String> ids = store.listPluginIds();
    assertEquals(1, ids.size());
    assertEquals("jpa.p", ids.get(0));

    Path loaded = store.getPluginPath("jpa.p");
    assertNotNull(loaded);
    assertEquals(3, Files.readAllBytes(loaded).length);

    assertTrue(store.deletePlugin("jpa.p"));
    assertFalse(repository.existsById("jpa.p"));
    assertNull(store.getPluginPath("jpa.p"));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @EntityScan(basePackageClasses = PluginEntity.class)
  @EnableJpaRepositories(basePackageClasses = PluginRepository.class)
  static class TestJpaApplication {

  }
}
