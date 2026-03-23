package cloud.xcan.angus.cache.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CachePropertiesTest {

  @Test
  void defaults() {
    CacheProperties p = new CacheProperties();
    assertNotNull(p.getMemory());
    assertEquals(10_000L, p.getMemory().getMaxSize());
    assertEquals(300L, p.getMemory().getCleanupIntervalSeconds());
    assertNotNull(p.getManagement());
    assertFalse(p.getManagement().isEnabled());
  }

  @Test
  void nestedSetters() {
    CacheProperties p = new CacheProperties();
    p.getMemory().setMaxSize(500);
    p.getMemory().setCleanupIntervalSeconds(120);
    p.getManagement().setEnabled(true);
    assertEquals(500L, p.getMemory().getMaxSize());
    assertEquals(120L, p.getMemory().getCleanupIntervalSeconds());
    assertTrue(p.getManagement().isEnabled());
  }
}
