package cloud.xcan.angus.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Caffeine-based In-Memory Cache Implementation.
 * High-performance cache using Caffeine library with proper LRU eviction policy.
 * This replaces the manual ConcurrentHashMap implementation to fix eviction issues
 * and improve performance.
 */
@Slf4j
public class CaffeineMemoryCache {

  private static class CacheItem {
    String value;
    LocalDateTime expireAt;

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

  public CaffeineMemoryCache(long maxSize, long cleanupIntervalSeconds) {
    this.maxSize = maxSize;
    this.cache = Caffeine.newBuilder()
        .maximumSize(maxSize)
        // Properly implement LRU based on access time, not hit count
        .expireAfterAccess(Duration.ofSeconds(cleanupIntervalSeconds))
        // Enable statistics for monitoring
        .recordStats()
        .build();
    
    log.info("CaffeineMemoryCache initialized with maxSize={}, cleanupInterval={}s", 
             maxSize, cleanupIntervalSeconds);
  }

  /**
   * Put a value in memory cache
   */
  public void put(String key, String value, LocalDateTime expireAt) {
    cache.put(key, new CacheItem(value, expireAt));
  }

  /**
   * Get a value from memory cache
   */
  public Optional<String> get(String key) {
    CacheItem item = cache.getIfPresent(key);
    if (item == null) {
      return Optional.empty();
    }

    if (item.isExpired()) {
      cache.invalidate(key);
      return Optional.empty();
    }

    return Optional.of(item.value);
  }

  /**
   * Check if key exists in memory cache
   */
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

  /**
   * Remove a key from memory cache
   */
  public void remove(String key) {
    cache.invalidate(key);
  }

  /**
   * Clear all cache
   */
  public void clear() {
    cache.invalidateAll();
  }

  /**
   * Get cache size
   */
  public long size() {
    return cache.estimatedSize();
  }

  /**
   * Get hit rate from Caffeine statistics
   */
  public double getHitRate() {
    CacheStats stats = cache.stats();
    return stats.hitRate();
  }

  /**
   * Get cache stats
   */
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
