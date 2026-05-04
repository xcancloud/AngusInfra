package cloud.xcan.angus.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CacheStatsTest {

  @Test
  void builder_populatesFields() {
    CacheStats s = CacheStats.builder()
        .totalEntries(1)
        .expiredEntries(2)
        .activeEntries(3)
        .memorySize(4)
        .databaseSize(5)
        .hits(6)
        .misses(7)
        .hitRate(0.5)
        .build();
    assertEquals(1, s.getTotalEntries());
    assertEquals(2, s.getExpiredEntries());
    assertEquals(3, s.getActiveEntries());
    assertEquals(4, s.getMemorySize());
    assertEquals(5, s.getDatabaseSize());
    assertEquals(6, s.getHits());
    assertEquals(7, s.getMisses());
    assertEquals(0.5, s.getHitRate());
  }
}
