package cloud.xcan.angus.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.entry.CacheEntry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HybridCacheManagerTest {

  private HybridCacheManager manager;
  private ConcurrentHashMap<String, CacheEntry> store;

  @BeforeEach
  void setUp() {
    store = new ConcurrentHashMap<>();
    CachePersistence persistence = Mockito.mock(CachePersistence.class);

    when(persistence.findByKey(anyString())).thenAnswer(
        inv -> Optional.ofNullable(store.get(inv.getArgument(0))));

    when(persistence.save(any())).thenAnswer(inv -> {
      CacheEntry e = inv.getArgument(0);
      store.put(e.getKey(), e);
      return e;
    });

    when(persistence.deleteByKey(anyString())).thenAnswer(inv -> {
      String k = inv.getArgument(0);
      return store.remove(k) != null;
    });

    doAnswer(inv -> {
      store.clear();
      return null;
    }).when(persistence).deleteAll();

    when(persistence.count()).thenAnswer(inv -> (long) store.size());

    when(persistence.countExpiredEntries()).thenAnswer(
        inv -> store.values().stream().filter(CacheEntry::hasExpired).count());

    when(persistence.deleteExpiredEntries()).thenAnswer(inv -> {
      int before = store.size();
      store.entrySet().removeIf(e -> e.getValue().hasExpired());
      return before - store.size();
    });

    // Use the non-deprecated constructor with explicit CacheProperties
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(1000);
    props.getMemory().setCleanupIntervalSeconds(60);
    manager = new HybridCacheManager(persistence, props);
  }

  @Test
  void testSetAndGet() {
    manager.set("k1", "v1", null);
    Optional<String> v = manager.get("k1");
    assertTrue(v.isPresent());
    assertEquals("v1", v.get());
  }

  @Test
  void testSetWithTtlAndGet() {
    manager.set("k_ttl", "v_ttl", 60L);
    Optional<String> v = manager.get("k_ttl");
    assertTrue(v.isPresent());
    assertEquals("v_ttl", v.get());
    assertTrue(manager.getTTL("k_ttl") > 0);
  }

  @Test
  void testExpiredEntryNotReturned() throws InterruptedException {
    manager.set("k2", "v2", 1L);
    long ttl = manager.getTTL("k2");
    assertTrue(ttl <= 1 && ttl >= 0);
    Thread.sleep(1200);
    Optional<String> v = manager.get("k2");
    assertFalse(v.isPresent());
  }

  @Test
  void testDelete_existingKey_returnsTrue() {
    manager.set("k3", "v3", null);
    assertTrue(manager.exists("k3"));
    assertTrue(manager.delete("k3"));
    assertFalse(manager.exists("k3"));
  }

  @Test
  void testDelete_nonExistingKey_returnsFalse() {
    assertFalse(manager.delete("no_such_key"));
  }

  @Test
  void testClear() {
    manager.set("k4", "v4", null);
    manager.clear();
    assertFalse(manager.exists("k4"));
  }

  @Test
  void testCleanupExpiredEntries() throws InterruptedException {
    manager.set("k5", "v5", 1L);
    Thread.sleep(1200);
    int deleted = manager.cleanupExpiredEntries();
    assertTrue(deleted >= 1);
  }

  @Test
  void testExpire_extendsTTL() {
    manager.set("k6", "v6", 60L);
    boolean ok = manager.expire("k6", 120L);
    assertTrue(ok);
    long ttl = manager.getTTL("k6");
    assertTrue(ttl > 60);
  }

  @Test
  void testExpire_unknownKey_returnsFalse() {
    assertFalse(manager.expire("no_such_key", 60L));
  }

  @Test
  void testGetStats() {
    manager.set("k7", "v7", null);
    CacheStats stats = manager.getStats();
    assertTrue(stats.getTotalEntries() >= 1);
    assertTrue(stats.getActiveEntries() >= 0);
  }

  @Test
  void testTTL_noExpiration_returnsMinusOne() {
    manager.set("k8", "v8", null);
    long ttl = manager.getTTL("k8");
    assertEquals(-1L, ttl);
  }

  @Test
  void testTTL_unknownKey_returnsMinusTwo() {
    long ttl = manager.getTTL("unknown_key");
    assertEquals(-2L, ttl);
  }
}

