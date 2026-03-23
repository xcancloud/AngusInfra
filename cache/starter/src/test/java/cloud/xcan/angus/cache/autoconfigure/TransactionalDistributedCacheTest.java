package cloud.xcan.angus.cache.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionalDistributedCacheTest {

  private IDistributedCache delegate;
  private TransactionalDistributedCache cache;

  @BeforeEach
  void setUp() {
    delegate = Mockito.mock(IDistributedCache.class);
    cache = new TransactionalDistributedCache(delegate);
  }

  @Test
  void delegatesAllOperations() {
    when(delegate.get("k")).thenReturn(Optional.of("v"));
    when(delegate.delete("d")).thenReturn(true);
    when(delegate.exists("e")).thenReturn(true);
    when(delegate.getTTL("t")).thenReturn(5L);
    when(delegate.expire("x", 10L)).thenReturn(true);
    when(delegate.getStats()).thenReturn(CacheStats.builder().totalEntries(1).build());
    when(delegate.cleanupExpiredEntries()).thenReturn(3);

    cache.set("a", "b", 1L);
    cache.set("a2", "b2");
    assertEquals(Optional.of("v"), cache.get("k"));
    assertTrue(cache.delete("d"));
    assertTrue(cache.exists("e"));
    assertEquals(5L, cache.getTTL("t"));
    assertTrue(cache.expire("x", 10L));
    cache.clear();
    assertEquals(1L, cache.getStats().getTotalEntries());
    assertEquals(3, cache.cleanupExpiredEntries());

    verify(delegate).set("a", "b", 1L);
    verify(delegate).set("a2", "b2");
    verify(delegate).get("k");
    verify(delegate).delete("d");
    verify(delegate).exists("e");
    verify(delegate).getTTL("t");
    verify(delegate).expire("x", 10L);
    verify(delegate).clear();
    verify(delegate).getStats();
    verify(delegate).cleanupExpiredEntries();
  }
}
