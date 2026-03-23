package cloud.xcan.angus.cache.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.cache.entry.CacheEntry;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoOpCachePersistenceTest {

  private NoOpCachePersistence persistence;

  @BeforeEach
  void setUp() {
    persistence = new NoOpCachePersistence();
  }

  @Test
  void saveAndFind() {
    CacheEntry e = entry("a", "1", LocalDateTime.now().plusHours(1));
    persistence.save(e);
    Optional<CacheEntry> found = persistence.findByKey("a");
    assertTrue(found.isPresent());
    assertEquals("1", found.get().getValue());
  }

  @Test
  void deleteByKey() {
    persistence.save(entry("b", "v", null));
    assertTrue(persistence.deleteByKey("b"));
    assertTrue(persistence.findByKey("b").isEmpty());
    assertFalse(persistence.deleteByKey("b"));
  }

  @Test
  void deleteAll() {
    persistence.save(entry("c", "v", null));
    persistence.deleteAll();
    assertEquals(0, persistence.count());
  }

  @Test
  void countAndExpiredHelpers() {
    persistence.save(entry("ok", "v", LocalDateTime.now().plusHours(1)));
    persistence.save(entry("gone", "v", LocalDateTime.now().minusSeconds(1)));
    assertEquals(2, persistence.count());
    assertEquals(1, persistence.countExpiredEntries());
    int removed = persistence.deleteExpiredEntries();
    assertEquals(1, removed);
    assertEquals(1, persistence.count());
  }

  private static CacheEntry entry(String key, String value, LocalDateTime expireAt) {
    return CacheEntry.builder()
        .key(key)
        .value(value)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(expireAt)
        .build();
  }
}
