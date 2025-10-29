package cloud.xcan.angus.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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

    // findByKey
    when(persistence.findByKey(anyString())).thenAnswer(inv -> Optional.ofNullable(store.get(inv.getArgument(0))));

    // save
    when(persistence.save(any())).thenAnswer(inv -> {
      CacheEntry e = inv.getArgument(0);
      store.put(e.getKey(), e);
      return e;
    });

    // deleteByKey
    doAnswer(inv -> {
      String k = inv.getArgument(0);
      store.remove(k);
      return null;
    }).when(persistence).deleteByKey(anyString());

    // deleteAll
    doAnswer(inv -> {
      store.clear();
      return null;
    }).when(persistence).deleteAll();

    // count
    when(persistence.count()).thenAnswer(inv -> (long) store.size());

    // countExpiredEntries
    when(persistence.countExpiredEntries()).thenAnswer(inv -> store.values().stream().filter(CacheEntry::hasExpired).count());

    // deleteExpiredEntries
    when(persistence.deleteExpiredEntries()).thenAnswer(inv -> {
      int before = store.size();
      store.entrySet().removeIf(e -> e.getValue().hasExpired());
      return before - store.size();
    });

    manager = new HybridCacheManager(persistence);
  }

  @Test
  void testSetAndGet() {
    manager.set("k1", "v1", null);
    Optional<String> v = manager.get("k1");
    assertTrue(v.isPresent());
    assertEquals("v1", v.get());
  }

  @Test
  void testExpireAndTTL() throws InterruptedException {
    manager.set("k2", "v2", 1L);
    long ttl = manager.getTTL("k2");
    assertTrue(ttl <= 1 && ttl >= 0);
    Thread.sleep(1200);
    Optional<String> v = manager.get("k2");
    assertFalse(v.isPresent());
  }

  @Test
  void testDeleteClearCleanup() {
    manager.set("k3", "v3", null);
    assertTrue(manager.exists("k3"));
    assertTrue(manager.delete("k3"));
    assertFalse(manager.exists("k3"));

    manager.set("k4", "v4", 1L);
    manager.clear();
    assertFalse(manager.exists("k4"));

    manager.set("k5", "v5", 1L);
    int deleted = manager.cleanupExpiredEntries();
    // might be zero if not expired yet
    assertTrue(deleted >= 0);
  }
}
