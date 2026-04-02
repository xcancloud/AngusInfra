package cloud.xcan.angus.cache.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CacheEntryTest {

  @Test
  void hasExpired_nullExpireAt_returnsFalse() {
    CacheEntry e = CacheEntry.builder()
        .key("k")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(null)
        .build();
    assertFalse(e.hasExpired());
  }

  @Test
  void hasExpired_futureExpireAt_returnsFalse() {
    CacheEntry e = CacheEntry.builder()
        .key("k")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().plusHours(1))
        .build();
    assertFalse(e.hasExpired());
  }

  @Test
  void hasExpired_pastExpireAt_returnsTrue() {
    CacheEntry e = CacheEntry.builder()
        .key("k")
        .value("v")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .expireAt(LocalDateTime.now().minusSeconds(1))
        .build();
    assertTrue(e.hasExpired());
  }

  @Test
  void setTTL_setsSecondsAndExpireAt() {
    CacheEntry e = new CacheEntry();
    e.setTTL(120L);
    assertEquals(120L, e.getTtlSeconds());
    assertTrue(e.getExpireAt().isAfter(LocalDateTime.now()));
  }
}
