package cloud.xcan.angus.cache;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * In-Memory Cache Implementation Fast cache layer using ConcurrentHashMap
 */
@Slf4j
public class MemoryCache {

  private static class CacheItem {

    String value;
    LocalDateTime expireAt;
    AtomicLong hits = new AtomicLong(0);

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

  private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
  private final long maxSize;
  private final long cleanupIntervalSeconds;
  private final AtomicLong hits = new AtomicLong(0);
  private final AtomicLong misses = new AtomicLong(0);
  private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
      r -> {
        Thread t = new Thread(r, "MemoryCache-Cleanup");
        t.setDaemon(true);
        return t;
      });

  public MemoryCache(long maxSize, long cleanupIntervalSeconds) {
    this.maxSize = maxSize;
    this.cleanupIntervalSeconds = cleanupIntervalSeconds;
    startCleanupTask();
  }

  /**
   * Put a value in memory cache
   */
  public void put(String key, String value, LocalDateTime expireAt) {
    if (cache.size() >= maxSize) {
      evictOldest();
    }
    cache.put(key, new CacheItem(value, expireAt));
  }

  /**
   * Get a value from memory cache
   */
  public Optional<String> get(String key) {
    CacheItem item = cache.get(key);
    if (item == null) {
      misses.incrementAndGet();
      return Optional.empty();
    }

    if (item.isExpired()) {
      cache.remove(key);
      misses.incrementAndGet();
      return Optional.empty();
    }

    item.hits.incrementAndGet();
    hits.incrementAndGet();
    return Optional.of(item.value);
  }

  /**
   * Check if key exists in memory cache
   */
  public boolean containsKey(String key) {
    CacheItem item = cache.get(key);
    if (item == null) {
      return false;
    }
    if (item.isExpired()) {
      cache.remove(key);
      return false;
    }
    return true;
  }

  /**
   * Remove a key from memory cache
   */
  public void remove(String key) {
    cache.remove(key);
  }

  /**
   * Clear all cache
   */
  public void clear() {
    cache.clear();
  }

  /**
   * Get cache size
   */
  public long size() {
    return cache.size();
  }

  /**
   * Get hit rate
   */
  public double getHitRate() {
    long h = hits.get();
    long m = misses.get();
    long total = h + m;
    return total == 0 ? 0 : (double) h / total;
  }

  /**
   * Get cache stats
   */
  public Map<String, Object> getStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("size", cache.size());
    stats.put("hits", hits.get());
    stats.put("misses", misses.get());
    stats.put("hitRate", getHitRate());
    return stats;
  }

  /**
   * Schedule periodic cleanup of expired entries
   */
  private void startCleanupTask() {
    cleanupExecutor.scheduleAtFixedRate(this::cleanup, cleanupIntervalSeconds,
        cleanupIntervalSeconds, TimeUnit.SECONDS);
  }

  /**
   * Remove expired entries
   */
  private void cleanup() {
    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    log.debug("Memory cache cleanup completed. Remaining entries: {}", cache.size());
  }

  /**
   * Evict oldest entry when cache is full
   */
  private void evictOldest() {
    cache.entrySet().stream()
        .min(Comparator.comparingLong(e -> e.getValue().hits.get()))
        .ifPresent(entry -> {
          cache.remove(entry.getKey());
          log.debug("Evicted cache entry: {}", entry.getKey());
        });
  }
}
