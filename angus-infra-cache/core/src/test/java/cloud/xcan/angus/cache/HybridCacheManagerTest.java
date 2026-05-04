package cloud.xcan.angus.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.entity.CacheEntry;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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

  @Test
  @SuppressWarnings("deprecation")
  void deprecatedConstructor_usesDefaultProperties() {
    HybridCacheManager legacy = new HybridCacheManager(
        Mockito.mock(CachePersistence.class));
    assertTrue(legacy instanceof IDistributedCache);
  }

  @Test
  void setTwoArg_delegatesToThreeArgWithNullTtl() {
    CachePersistence persistence = Mockito.mock(CachePersistence.class);
    when(persistence.findByKey(anyString())).thenReturn(Optional.empty());
    when(persistence.save(any())).thenAnswer(inv -> inv.getArgument(0));
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(persistence, props);
    m.set("plain", "val");
    Mockito.verify(persistence).save(Mockito.argThat(
        e -> "plain".equals(((CacheEntry) e).getKey())
            && "val".equals(((CacheEntry) e).getValue())
            && ((CacheEntry) e).getExpireAt() == null));
  }

  @Test
  void get_loadsFromPersistenceWhenMemoryMiss() {
    CacheEntry dbOnly = CacheEntry.builder()
        .key("dbkey")
        .value("from-db")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().plusMinutes(5))
        .ttlSeconds(300L)
        .isExpired(false)
        .build();
    store.put("dbkey", dbOnly);
    Optional<String> v = manager.get("dbkey");
    assertTrue(v.isPresent());
    assertEquals("from-db", v.get());
    assertTrue(manager.get("dbkey").isPresent());
  }

  @Test
  void get_expiredDbEntry_returnsEmpty() {
    CacheEntry expired = CacheEntry.builder()
        .key("ex")
        .value("x")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().minusSeconds(1))
        .ttlSeconds(1L)
        .isExpired(false)
        .build();
    store.put("ex", expired);
    assertTrue(manager.get("ex").isEmpty());
  }

  @Test
  void exists_checksPersistenceWhenNotInMemory() {
    CacheEntry e = CacheEntry.builder()
        .key("e1")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().plusHours(1))
        .build();
    store.put("e1", e);
    assertTrue(manager.exists("e1"));
  }

  @Test
  void getTTL_expiredEntry_returnsMinusTwo() {
    CacheEntry expired = CacheEntry.builder()
        .key("t")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().minusSeconds(1))
        .build();
    store.put("t", expired);
    assertEquals(-2L, manager.getTTL("t"));
  }

  @Test
  void set_persistenceFailure_stillServesFromMemory() {
    CachePersistence failing = Mockito.mock(CachePersistence.class);
    when(failing.findByKey(anyString())).thenReturn(Optional.empty());
    doThrow(new RuntimeException("db down")).when(failing).save(any());
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(failing, props);
    m.set("k", "mem-only", null);
    assertEquals(Optional.of("mem-only"), m.get("k"));
  }

  @Test
  void delete_persistenceFailure_returnsTrueAfterMemoryCleared() {
    CachePersistence failing = Mockito.mock(CachePersistence.class);
    when(failing.findByKey(anyString())).thenReturn(Optional.empty());
    when(failing.save(any())).thenAnswer(inv -> inv.getArgument(0));
    doThrow(new RuntimeException("db")).when(failing).deleteByKey("d");
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(failing, props);
    m.set("d", "v", null);
    assertTrue(m.delete("d"));
  }

  @Test
  void clear_persistenceFailure_memoryStillCleared() {
    CachePersistence failing = Mockito.mock(CachePersistence.class);
    when(failing.findByKey(anyString())).thenReturn(Optional.empty());
    when(failing.save(any())).thenAnswer(inv -> inv.getArgument(0));
    doThrow(new RuntimeException("db")).when(failing).deleteAll();
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(failing, props);
    m.set("c", "v", null);
    m.clear();
    assertFalse(m.exists("c"));
  }

  @Test
  void expire_persistenceFailure_returnsFalse() {
    CachePersistence failing = Mockito.mock(CachePersistence.class);
    CacheEntry e = CacheEntry.builder()
        .key("x")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().plusHours(1))
        .build();
    when(failing.findByKey("x")).thenReturn(Optional.of(e));
    doThrow(new RuntimeException("db")).when(failing).save(any());
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(failing, props);
    assertFalse(m.expire("x", 10L));
  }

  @Test
  void cleanupExpiredEntries_persistenceFailure_returnsZero() {
    CachePersistence failing = Mockito.mock(CachePersistence.class);
    doThrow(new RuntimeException("db")).when(failing).deleteExpiredEntries();
    CacheProperties props = new CacheProperties();
    props.getMemory().setMaxSize(100);
    props.getMemory().setCleanupIntervalSeconds(60);
    HybridCacheManager m = new HybridCacheManager(failing, props);
    assertEquals(0, m.cleanupExpiredEntries());
  }

  @Test
  void getStats_usesZeroWhenMemoryStatsNotNumeric() throws Exception {
    Map<String, Object> bad = new HashMap<>();
    bad.put("hits", "x");
    bad.put("misses", "y");
    bad.put("hitRate", "z");
    CaffeineMemoryCache broken = new CaffeineMemoryCache(10, 60) {
      @Override
      public Map<String, Object> getStats() {
        return bad;
      }
    };
    Field f = HybridCacheManager.class.getDeclaredField("memoryCache");
    f.setAccessible(true);
    f.set(manager, broken);
    CacheStats stats = manager.getStats();
    assertEquals(0L, stats.getHits());
    assertEquals(0L, stats.getMisses());
    assertEquals(0.0, stats.getHitRate());
  }

  @Test
  void set_updatesExistingPersistenceEntry() {
    CacheEntry existing = CacheEntry.builder()
        .key("upd")
        .value("old")
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .expireAt(null)
        .build();
    store.put("upd", existing);
    manager.set("upd", "new", null);
    assertEquals("new", store.get("upd").getValue());
  }
}

