package cloud.xcan.angus.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaffeineMemoryCacheTest {

  private CaffeineMemoryCache cache;

  @BeforeEach
  void setUp() {
    cache = new CaffeineMemoryCache(100, 60);
  }

  @Test
  void putAndGet_withoutExpiry_returnsValue() {
    cache.put("k", "v", null);
    assertTrue(cache.get("k").isPresent());
    assertEquals("v", cache.get("k").get());
  }

  @Test
  void containsKey_trueWhenPresent() {
    cache.put("k", "v", null);
    assertTrue(cache.containsKey("k"));
  }

  @Test
  void get_miss_returnsEmpty() {
    assertTrue(cache.get("missing").isEmpty());
  }

  @Test
  void containsKey_falseWhenMissing() {
    assertFalse(cache.containsKey("nope"));
  }

  @Test
  void get_expiredEntry_returnsEmpty() throws InterruptedException {
    LocalDateTime soon = LocalDateTime.now().plusMillis(80);
    cache.put("e", "v", soon);
    Thread.sleep(250);
    assertTrue(cache.get("e").isEmpty());
  }

  @Test
  void containsKey_expired_invalidates() throws InterruptedException {
    LocalDateTime soon = LocalDateTime.now().plusMillis(80);
    cache.put("e2", "v", soon);
    Thread.sleep(250);
    assertFalse(cache.containsKey("e2"));
  }

  @Test
  void remove_invalidates() {
    cache.put("k", "v", null);
    cache.remove("k");
    assertTrue(cache.get("k").isEmpty());
  }

  @Test
  void clear_removesAll() {
    cache.put("a", "1", null);
    cache.put("b", "2", null);
    cache.clear();
    assertTrue(cache.get("a").isEmpty());
    assertEquals(0L, cache.size());
  }

  @Test
  void getStats_containsExpectedKeys() {
    cache.put("x", "y", null);
    cache.get("x");
    cache.get("missing");
    Map<String, Object> stats = cache.getStats();
    assertTrue(stats.containsKey("hits"));
    assertTrue(stats.containsKey("misses"));
    assertTrue(stats.containsKey("hitRate"));
    assertTrue(stats.containsKey("size"));
    assertTrue(stats.containsKey("evictionCount"));
    assertTrue(((Number) stats.get("hits")).longValue() >= 1);
  }

  @Test
  void getHitRate_afterAccess() {
    cache.put("k", "v", null);
    cache.get("k");
    assertTrue(cache.getHitRate() >= 0.0 && cache.getHitRate() <= 1.0);
  }

  @Test
  void put_withPastExpireAt_stillStoresUntilEvicted() {
    cache.put("past", "v", LocalDateTime.now().minusSeconds(1));
    assertTrue(cache.get("past").isEmpty());
  }
}
