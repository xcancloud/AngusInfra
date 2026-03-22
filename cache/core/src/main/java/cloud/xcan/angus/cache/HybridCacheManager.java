package cloud.xcan.angus.cache;

import cloud.xcan.angus.cache.config.CacheProperties;
import cloud.xcan.angus.cache.entry.CacheEntry;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Hybrid Cache Manager Combines in-memory cache with database persistence
 */
@Slf4j
public class HybridCacheManager implements IDistributedCache {

  private final CaffeineMemoryCache memoryCache;
  private final CachePersistence cachePersistence;

  public HybridCacheManager(CachePersistence cachePersistence, CacheProperties cacheProperties) {
    this.cachePersistence = cachePersistence;
    // Initialize memory cache with configured values using Caffeine
    this.memoryCache = new CaffeineMemoryCache(
        cacheProperties.getMemory().getMaxSize(),
        cacheProperties.getMemory().getCleanupIntervalSeconds()
    );
  }

  /**
   * Constructor with default configuration for backward compatibility
   *
   * @deprecated Use {@link #HybridCacheManager(CachePersistence, CacheProperties)} instead
   */
  @Deprecated
  public HybridCacheManager(CachePersistence cachePersistence) {
    this(cachePersistence, createDefaultProperties());
  }

  private static CacheProperties createDefaultProperties() {
    CacheProperties properties = new CacheProperties();
    properties.getMemory().setMaxSize(10000);
    properties.getMemory().setCleanupIntervalSeconds(300);
    return properties;
  }

  /**
   * Set cache value with TTL
   */
  @Override
  public void set(String key, String value, Long ttlSeconds) {
    LocalDateTime expireAt = ttlSeconds != null
        ? LocalDateTime.now().plusSeconds(ttlSeconds)
        : null;

    // Always save to memory cache first (fast path)
    memoryCache.put(key, value, expireAt);

    // Try to persist to database (best effort, with degradation strategy)
    try {
      CacheEntry entry = cachePersistence.findByKey(key).orElse(null);
      if (entry == null) {
        entry = CacheEntry.builder()
            .key(key)
            .value(value)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .expireAt(expireAt)
            .ttlSeconds(ttlSeconds)
            .isExpired(false)
            .build();
      } else {
        entry.setValue(value);
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setExpireAt(expireAt);
        entry.setTtlSeconds(ttlSeconds);
        entry.setIsExpired(false);
      }
      cachePersistence.save(entry);
      log.debug("Cache set: key={}, ttl={}, persisted=true", key, ttlSeconds);
    } catch (Exception e) {
      // Degradation: DB persistence failed but memory cache still works
      // Service continues with memory-only mode
      log.error(
          "[CACHE-DEGRADATION] Failed to persist cache to DB, falling back to memory-only mode. " +
              "key={}, error={}", key, e.getMessage(), e);
      // TODO: Emit metric for monitoring: cache_db_persistence_failures_total
      //MemoryCache continues to work - no exception thrown
    }
  }

  /**
   * Set cache value without expiration
   */
  @Override
  public void set(String key, String value) {
    set(key, value, null);
  }

  /**
   * Get cache value
   */
  @Override
  public Optional<String> get(String key) {
    // Try memory cache first
    Optional<String> memoryValue = memoryCache.get(key);
    if (memoryValue.isPresent()) {
      log.debug("Cache hit (memory): key={}", key);
      return memoryValue;
    }

    // Fall back to database
    Optional<CacheEntry> dbEntry = cachePersistence.findByKey(key);
    if (dbEntry.isPresent()) {
      CacheEntry entry = dbEntry.get();

      if (entry.hasExpired()) {
        // Do NOT call delete() here: that would require a write inside a potentially
        // read-only transaction context. The periodic cleanupExpiredEntries() will purge it.
        log.debug("Cache expired (db): key={}", key);
        return Optional.empty();
      }

      // Warm the memory cache from DB
      memoryCache.put(key, entry.getValue(), entry.getExpireAt());
      log.debug("Cache hit (database): key={}", key);
      return Optional.of(entry.getValue());
    }

    log.debug("Cache miss: key={}", key);
    return Optional.empty();
  }

  @Override
  public boolean delete(String key) {
    memoryCache.remove(key);
    try {
      boolean deleted = cachePersistence.deleteByKey(key);
      log.debug("Cache deleted: key={}, found={}", key, deleted);
      return deleted;
    } catch (Exception e) {
      log.error("[CACHE-DEGRADATION] Failed to delete cache from DB, memory already cleared. "
          + "key={}, error={}", key, e.getMessage(), e);
      return true; // memory was cleared — treat as deleted
    }
  }

  /**
   * Check if key exists and not expired
   */
  @Override
  public boolean exists(String key) {
    if (memoryCache.containsKey(key)) {
      return true;
    }

    Optional<CacheEntry> entry = cachePersistence.findByKey(key);
    if (entry.isPresent()) {
      return !entry.get().hasExpired();
    }
    return false;
  }

  /**
   * Get TTL for a key
   */
  @Override
  public long getTTL(String key) {
    Optional<CacheEntry> entry = cachePersistence.findByKey(key);
    if (!entry.isPresent()) {
      return -2; // Key not found
    }

    CacheEntry cacheEntry = entry.get();
    if (cacheEntry.hasExpired()) {
      return -2; // Expired
    }

    if (cacheEntry.getExpireAt() == null) {
      return -1; // No expiration
    }

    long ttl = java.time.temporal.ChronoUnit.SECONDS.between(
        LocalDateTime.now(),
        cacheEntry.getExpireAt()
    );
    return Math.max(ttl, 0);
  }

  @Override
  public boolean expire(String key, long ttlSeconds) {
    Optional<CacheEntry> entry = cachePersistence.findByKey(key);
    if (!entry.isPresent()) {
      return false;
    }
    try {
      CacheEntry cacheEntry = entry.get();
      LocalDateTime expireAt = LocalDateTime.now().plusSeconds(ttlSeconds);
      cacheEntry.setExpireAt(expireAt);
      cacheEntry.setTtlSeconds(ttlSeconds);
      cacheEntry.setIsExpired(false);
      cachePersistence.save(cacheEntry);
      memoryCache.remove(key); // invalidate memory entry; refreshed on next get()
      log.debug("Cache expiration set: key={}, ttl={}", key, ttlSeconds);
      return true;
    } catch (Exception e) {
      log.error("[CACHE-DEGRADATION] Failed to set expiration in DB. key={}, error={}",
          key, e.getMessage(), e);
      return false;
    }
  }

  @Override
  public void clear() {
    memoryCache.clear();
    try {
      cachePersistence.deleteAll();
      log.info("All cache cleared");
    } catch (Exception e) {
      log.error("[CACHE-DEGRADATION] Failed to clear DB cache, memory cleared. error={}",
          e.getMessage(), e);
    }
  }

  /**
   * Get cache statistics
   */
  @Override
  public CacheStats getStats() {
    long totalEntries = cachePersistence.count();
    long expiredEntries = cachePersistence.countExpiredEntries();
    long activeEntries = totalEntries - expiredEntries;
    long memorySize = memoryCache.size();

    Map<String, Object> memStats = memoryCache.getStats();
    long hits =
        memStats.get("hits") instanceof Number ? ((Number) memStats.get("hits")).longValue() : 0L;
    long misses =
        memStats.get("misses") instanceof Number ? ((Number) memStats.get("misses")).longValue()
            : 0L;
    double hitRate =
        memStats.get("hitRate") instanceof Number ? ((Number) memStats.get("hitRate")).doubleValue()
            : 0.0;

    return CacheStats.builder()
        .totalEntries(totalEntries)
        .expiredEntries(expiredEntries)
        .activeEntries(activeEntries)
        .memorySize(memorySize)
        .databaseSize(totalEntries)
        .hits(hits)
        .misses(misses)
        .hitRate(hitRate)
        .build();
  }

  @Override
  public int cleanupExpiredEntries() {
    try {
      int deletedCount = cachePersistence.deleteExpiredEntries();
      log.info("Cleaned up {} expired cache entries", deletedCount);
      return deletedCount;
    } catch (Exception e) {
      log.error("[CACHE-DEGRADATION] Failed to cleanup expired entries. error={}",
          e.getMessage(), e);
      return 0;
    }
  }
}
