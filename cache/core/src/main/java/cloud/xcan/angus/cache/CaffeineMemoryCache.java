package cloud.xcan.angus.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Caffeine-based In-Memory Cache Implementation.
 * Uses per-entry TTL via {@link Expiry} so each cache item expires at its own
 * {@code expireAt} timestamp, independent of any global access-based eviction policy.
 */
@Slf4j
public class CaffeineMemoryCache {

  private static class CacheItem {
    final String value;
    final LocalDateTime expireAt;

    CacheItem(String value, LocalDateTime expireAt) {
      this.value = value;
      this.expireAt = expireAt;
    }

    boolean isExpired() {
      if (expireAt == null) {
        return false;
      }
      return LocalDateTime.now().isAfter(expireAt);
    }
  }

  private final Cache<String, CacheItem> cache;
  private final long maxSize;

  /**
   * @param maxSize                maximum number of entries held in memory (LRU eviction)
   * @param cleanupIntervalSeconds retained for API compatibility; Caffeine manages per-entry
   *                               expiry automatically via the {@code expireAt} timestamp,
   *                               so this parameter is no longer used in the Caffeine builder
   */
  public CaffeineMemoryCache(long maxSize, long cleanupIntervalSeconds) {
    this.maxSize = maxSize;
    this.cache = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfter(new Expiry<String, CacheItem>() {
          @Override
          public long expireAfterCreate(String key, CacheItem value, long currentTime) {
            if (value.expireAt == null) {
              return Long.MAX_VALUE; // no TTL — evicted by LRU only
            }
            Duration remaining = Duration.between(LocalDateTime.now(), value.expireAt);
            return remaining.isNegative() ? 0L : remaining.toNanos();
          }

          @Override
          public long expireAfterUpdate(String key, CacheItem value, long currentTime,
              long currentDuration) {
            return expireAfterCreate(key, value, currentTime);
          }

          @Override
          public long expireAfterRead(String key, CacheItem value, long currentTime,
              long currentDuration) {
            return currentDuration; // reading does not reset per-entry TTL
          }
        })
        .recordStats()
        .build();

    log.info("CaffeineMemoryCache initialized with maxSize={}", maxSize);
  }

  public void put(String key, String value, LocalDateTime expireAt) {
    cache.put(key, new CacheItem(value, expireAt));
  }

  public Optional<String> get(String key) {
    CacheItem item = cache.getIfPresent(key);
    if (item == null) {
      return Optional.empty();
    }
    // Defensive check: Caffeine also enforces TTL, but guard against clock skew
    if (item.isExpired()) {
      cache.invalidate(key);
      return Optional.empty();
    }
    return Optional.of(item.value);
  }

  public boolean containsKey(String key) {
    CacheItem item = cache.getIfPresent(key);
    if (item == null) {
      return false;
    }
    if (item.isExpired()) {
      cache.invalidate(key);
      return false;
    }
    return true;
  }

  public void remove(String key) {
    cache.invalidate(key);
  }

  public void clear() {
    cache.invalidateAll();
  }

  public long size() {
    return cache.estimatedSize();
  }

  public double getHitRate() {
    return cache.stats().hitRate();
  }

  public Map<String, Object> getStats() {
    CacheStats stats = cache.stats();
    Map<String, Object> statsMap = new HashMap<>();
    statsMap.put("size", cache.estimatedSize());
    statsMap.put("hits", stats.hitCount());
    statsMap.put("misses", stats.missCount());
    statsMap.put("hitRate", stats.hitRate());
    statsMap.put("evictionCount", stats.evictionCount());
    statsMap.put("loadSuccessCount", stats.loadSuccessCount());
    statsMap.put("loadFailureCount", stats.loadFailureCount());
    statsMap.put("totalLoadTime", stats.totalLoadTime());
    statsMap.put("averageLoadPenalty", stats.averageLoadPenalty());
    return statsMap;
  }
}
