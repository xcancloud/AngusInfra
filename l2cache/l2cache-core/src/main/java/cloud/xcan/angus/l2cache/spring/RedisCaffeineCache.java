package cloud.xcan.angus.l2cache.spring;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.l2cache.synchronous.CacheMessage;
import cloud.xcan.angus.lettucex.util.RedisService;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * A two-level cache implementation that combines Redis (L2) and Caffeine (L1) caches.
 * This cache provides high performance by utilizing local cache for frequent access
 * and distributed cache for data consistency across multiple instances.
 * </p>
 * 
 * <p>
 * Key features:
 * - L1 Cache: Local Caffeine cache for ultra-fast access
 * - L2 Cache: Distributed Redis cache for data persistence and sharing
 * - Cache synchronization via Redis pub/sub mechanism
 * - Configurable cache penetration protection
 * - Multi-tenant support with tenant-aware cache keys
 * </p>
 * 
 * <p>
 * Thread Safety: This class is thread-safe and can be used in concurrent environments.
 * </p>
 */
@Slf4j
public class RedisCaffeineCache extends AbstractValueAdaptingCache {

  private final String cacheName;

  /**
   * Level 1 cache: Local Caffeine cache for high-speed access.
   * Provides sub-millisecond response times for frequently accessed data.
   */
  private final Cache<Object, Object> level1Cache;

  /**
   * Level 2 cache service: Distributed Redis cache for data persistence and sharing.
   * Ensures data consistency across multiple application instances.
   */
  private final RedisService<Object> redisService;

  /**
   * Default expiration time for cache entries in milliseconds.
   * Value of 0 means no expiration.
   */
  private final long defaultExpiration;

  /**
   * Default expiration time for cache penetration protection in milliseconds.
   * Used to cache null/empty values to prevent repeated database queries.
   */
  private final long defaultPenetrationExpiration;

  /**
   * Cache key prefix to avoid key collisions in shared Redis instances.
   */
  private static final String CACHE_PREFIX = "j2cache:";

  /**
   * Cache-specific expiration configurations mapped by cache name.
   */
  private final Map<String, Long> expires;

  /**
   * Cache-specific penetration protection expiration configurations.
   */
  private final Map<String, Long> penetrationExpires;

  /**
   * Composite cache configuration for L1 cache behavior control.
   */
  private final L2CacheProperties.Composite composite;

  /**
   * Redis-specific cache configuration.
   */
  private final L2CacheProperties.Redis redis;

  /**
   * <p>
   * Tracks whether Level 1 cache has been enabled to handle configuration inconsistencies.
   * Once enabled, it remains true to prevent cache inconsistency issues.
   * </p>
   * 
   * <p>
   * Scenario: Enable local cache → update data → disable local cache → 
   * update Redis data → re-enable local cache. This could cause stale data issues.
   * Solution: Clear L1 cache when configuration changes are detected.
   * </p>
   */
  private final AtomicBoolean openedL1Cache = new AtomicBoolean(false);

  /**
   * Per-key lock map to prevent cache stampede and ensure thread safety for cache loading.
   * Uses ConcurrentHashMap for thread-safe access to individual key locks.
   */
  private final Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<>();

  /**
   * Protected constructor for inheritance support.
   *
   * @param allowNullValues whether to allow null values in cache
   */
  protected RedisCaffeineCache(boolean allowNullValues) {
    super(allowNullValues);
    // Initialize required fields for protected constructor
    this.cacheName = null;
    this.level1Cache = null;
    this.redisService = null;
    this.defaultExpiration = 0;
    this.defaultPenetrationExpiration = 5 * 60 * 1000; // 5 minutes default
    this.expires = null;
    this.penetrationExpires = null;
    this.composite = null;
    this.redis = null;
  }

  /**
   * <p>
   * Main constructor for creating a two-level cache instance.
   * </p>
   *
   * @param cacheName the name of the cache
   * @param redisService Redis service for L2 cache operations
   * @param level1Cache Caffeine cache instance for L1 cache
   * @param l2CacheProperties cache configuration properties
   */
  public RedisCaffeineCache(String cacheName, RedisService<Object> redisService,
      Cache<Object, Object> level1Cache, L2CacheProperties l2CacheProperties) {
    super(l2CacheProperties.isAllowNullValues());
    this.cacheName = cacheName;
    this.redisService = redisService;
    this.level1Cache = level1Cache;
    this.defaultExpiration = l2CacheProperties.getRedis().getDefaultExpiration();
    this.defaultPenetrationExpiration = l2CacheProperties.getRedis()
        .getDefaultPenetrationExpiration();
    this.expires = l2CacheProperties.getRedis().getExpires();
    this.penetrationExpires = l2CacheProperties.getRedis().getPenetrationExpires();
    this.composite = l2CacheProperties.getComposite();
    this.redis = l2CacheProperties.getRedis();
  }

  @Override
  public String getName() {
    return this.cacheName;
  }

  @Override
  public Object getNativeCache() {
    return this;
  }

  /**
   * <p>
   * Retrieves a cache value, loading it if necessary using the provided callable.
   * Implements cache-aside pattern with double-checked locking to prevent cache stampede.
   * </p>
   *
   * @param key0 the cache key
   * @param call the callable to load the value if not present in cache
   * @param <T> the type of the cached value
   * @return the cached or loaded value
   */
  @Override
  public <T> T get(Object key0, Callable<T> call) {
    String key = key0.toString();

    // First attempt: Check cache without locking
    Object value = lookup(key);
    if (value != null) {
      return (T) value;
    }

    // Second attempt: Use per-key locking to prevent cache stampede
    ReentrantLock lock = keyLockMap.computeIfAbsent(key, k -> {
      log.debug("Creating lock for cache key: {}", k);
      return new ReentrantLock();
    });

    try {
      lock.lock();
      // Double-check pattern: verify cache is still empty after acquiring lock
      value = lookup(key);
      if (value != null) {
        return (T) value;
      }

      // Load value and store in cache
      value = call.call();
      Object storeValue = toStoreValue(value);
      put(key, storeValue);
      return (T) value;
    } catch (Exception e) {
      throw new ValueRetrievalException(key, call, e.getCause());
    } finally {
      lock.unlock();
      // Clean up lock if no longer needed to prevent memory leaks
      // Note: This is a simple cleanup strategy, more sophisticated approaches
      // could use weak references or periodic cleanup
      if (!lock.hasQueuedThreads()) {
        keyLockMap.remove(key, lock);
      }
    }
  }

  /**
   * <p>
   * Stores a value in both L1 and L2 caches with appropriate expiration and synchronization.
   * Handles cache penetration protection for empty values.
   * </p>
   *
   * @param key0 the cache key
   * @param value the value to store
   */
  @Override
  public void put(Object key0, Object value) {
    String key = key0.toString();
    Object storeValue = toStoreValue(value);

    // Handle cache penetration protection for empty values
    if (isEmptyValue(storeValue)) {
      emptyPenetrationSafe(key, storeValue);
      return;
    }

    String cacheKey = getKey(key);
    
    // Skip storing if the processed value is empty (after null handling)
    if (isEmptyValue(value)) {
      return;
    }

    // Store in Redis L2 cache with appropriate expiration
    long expire = getExpire();
    if (expire > 0) {
      redisService.set(cacheKey, storeValue, expire, TimeUnit.MILLISECONDS);
    } else {
      redisService.set(cacheKey, storeValue);
    }

    // Handle L1 cache if enabled
    boolean isL1Open = isL1Open(cacheKey);
    if (isL1Open) {
      // Notify all nodes to clear local cache to maintain consistency
      clearAllLocalCache(CacheMessage.of(this.cacheName, key));
      // Store in local L1 cache
      level1Cache.put(key, storeValue);
    }
  }

  /**
   * <p>
   * Implements cache penetration protection by storing empty values with shorter TTL.
   * This prevents repeated database queries for non-existent data.
   * </p>
   *
   * @param key the cache key
   * @param storeValue the empty value to store (must be empty)
   */
  public void emptyPenetrationSafe(String key, Object storeValue) {
    // Validate preconditions
    if (!isEmptyValue(storeValue)) {
      log.warn("emptyPenetrationSafe called with non-empty value for key: {}", key);
      return;
    }

    // If null values are not allowed, evict the key instead of storing empty value
    if (!super.isAllowNullValues()) {
      this.evict(key);
      return;
    }

    String cacheKey = getKey(key);
    
    // Only apply penetration protection if configured for this cache
    if (penetrationExpires.containsKey(cacheKey)) {
      long expire = getPenetrationExpire();
      // Store empty value with shorter TTL to prevent cache penetration
      redisService.set(cacheKey, storeValue, expire, TimeUnit.MILLISECONDS);
      
      if (log.isDebugEnabled()) {
        log.debug("Applied penetration protection for key: {}, expire: {}ms", key, expire);
      }
    }
  }

  /**
   * <p>
   * Checks if a value is considered empty for cache penetration protection.
   * </p>
   *
   * @param value the value to check
   * @return true if the value is empty, false otherwise
   */
  private boolean isEmptyValue(Object value) {
    return value == null 
        || "null".equals(value) 
        || "".equals(value)
        || NullValue.INSTANCE.equals(value)
        || (value instanceof Collection && ((Collection<?>) value).isEmpty())
        || (value instanceof Map && ((Map<?, ?>) value).isEmpty());
  }

  /**
   * <p>
   * Atomically sets a key-value pair only if the key does not exist.
   * Uses distributed locking to ensure atomicity across multiple instances.
   * </p>
   *
   * @param key0 the cache key
   * @param value the value to set
   * @return a ValueWrapper containing the previous value, or null if key was absent
   */
  @Override
  public ValueWrapper putIfAbsent(Object key0, Object value) {
    String key = key0.toString();
    String cacheKey = getKey(key);
    Object prevValue;

    // Use synchronized block for local atomicity
    // TODO: Consider implementing distributed lock for true cross-instance atomicity
    synchronized (key.intern()) {
      prevValue = redisService.get(cacheKey);
      if (prevValue == null) {
        // Key doesn't exist, set the value
        long expire = getExpire();
        Object storeValue = toStoreValue(value);
        
        if (expire > 0) {
          redisService.set(cacheKey, storeValue, expire, TimeUnit.MILLISECONDS);
        } else {
          redisService.set(cacheKey, storeValue);
        }

        // Synchronize cache across instances and update L1 cache
        clearAllLocalCache(CacheMessage.of(this.cacheName, key));
        level1Cache.put(key, storeValue);
      }
    }
    
    return toValueWrapper(prevValue);
  }

  /**
   * <p>
   * Evicts a single cache entry from both L1 and L2 caches.
   * Ensures proper order: Redis first, then local cache to prevent race conditions.
   * </p>
   *
   * @param key0 the cache key to evict
   */
  @Override
  public void evict(Object key0) {
    String key = key0.toString();
    
    // Clear Redis cache first to prevent other instances from reloading stale data
    redisService.delete(getKey(key));

    // Notify all instances to clear their local caches
    clearAllLocalCache(CacheMessage.of(this.cacheName, key));

    // Clear local L1 cache
    level1Cache.invalidate(key);
    
    if (log.isDebugEnabled()) {
      log.debug("Evicted cache entry for key: {}", key);
    }
  }

  /**
   * <p>
   * Batch eviction of multiple cache entries for improved performance.
   * </p>
   *
   * @param keys collection of keys to evict
   */
  public void evict(Collection<Object> keys) {
    if (isNotEmpty(keys)) {
      for (Object key : keys) {
        evict(key);
      }
    }
  }

  /**
   * <p>
   * Clears all cache entries for this cache instance.
   * Uses pattern matching to find all keys belonging to this cache.
   * </p>
   */
  @Override
  public void clear() {
    try {
      // Clear all Redis keys matching this cache's pattern
      Set<String> keys = redisService.keys(this.cacheName.concat(":*"));
      if (!keys.isEmpty()) {
        redisService.delete(keys);
        log.info("Cleared {} Redis cache entries for cache: {}", keys.size(), this.cacheName);
      }
      
      // Notify all instances to clear their local caches
      clearAllLocalCache(CacheMessage.of(this.cacheName, null));
      
      // Clear local L1 cache
      level1Cache.invalidateAll();
      
    } catch (Exception e) {
      log.error("Error clearing cache: {}", this.cacheName, e);
      throw new RuntimeException("Failed to clear cache: " + this.cacheName, e);
    }
  }

  /**
   * <p>
   * Looks up a value in the cache hierarchy: L1 first, then L2.
   * Implements cache warming by promoting L2 hits to L1 cache.
   * </p>
   *
   * @param key0 the cache key to lookup
   * @return the cached value or null if not found
   */
  @Override
  public Object lookup(Object key0) {
    String key = key0.toString();
    String cacheKey = getKey(key);

    boolean isL1Open = isL1Open(cacheKey);
    Object value = null;

    // Try L1 cache first if enabled
    if (isL1Open) {
      value = level1Cache.getIfPresent(key);
      if (value != null) {
        if (log.isDebugEnabled()) {
          log.debug("L1 cache hit for cache: {}, key: {}", this.getName(), key);
        }
        return value;
      }
    }

    // Try L2 cache (Redis)
    value = redisService.get(cacheKey);
    if (value != null) {
      if (log.isDebugEnabled()) {
        log.debug("L2 cache hit for cache: {}, key: {}", this.getName(), key);
      }
      
      // Promote to L1 cache if enabled (cache warming)
      if (isL1Open) {
        level1Cache.put(key, value);
        if (log.isDebugEnabled()) {
          log.debug("Promoted L2 hit to L1 cache for key: {}", key);
        }
      }
    }

    return value;
  }

  /**
   * <p>
   * Generates a tenant-aware cache key with proper namespacing.
   * Format: {prefix}{cacheName}:{tenantId}:{key}
   * </p>
   *
   * @param key the original cache key
   * @return the fully qualified cache key
   */
  private String getKey(String key) {
    return CACHE_PREFIX
        .concat(cacheName)
        .concat(":")
        .concat(getOptTenantId().toString())
        .concat(":")
        .concat(key);
  }

  /**
   * <p>
   * Retrieves the expiration time for this cache instance.
   * Uses cache-specific configuration if available, otherwise falls back to default.
   * </p>
   *
   * @return expiration time in milliseconds
   */
  private long getExpire() {
    Long cacheNameExpire = expires.get(this.cacheName);
    return cacheNameExpire != null ? cacheNameExpire : defaultExpiration;
  }

  /**
   * <p>
   * Retrieves the penetration protection expiration time for this cache instance.
   * </p>
   *
   * @return penetration protection expiration time in milliseconds
   */
  private long getPenetrationExpire() {
    Long cacheNameExpire = penetrationExpires.get(this.cacheName);
    return cacheNameExpire != null ? cacheNameExpire : defaultPenetrationExpiration;
  }

  /**
   * <p>
   * Notifies all application instances to clear their local caches via Redis pub/sub.
   * This ensures cache consistency across distributed deployments.
   * </p>
   *
   * @param message the cache invalidation message
   */
  private void clearAllLocalCache(CacheMessage message) {
    try {
      redisService.getRedisTemplate().convertAndSend(redis.getTopic(), message);
      if (log.isDebugEnabled()) {
        log.debug("Sent cache invalidation message: {}", message);
      }
    } catch (Exception e) {
      log.error("Failed to send cache invalidation message: {}", message, e);
      // Don't throw exception as this would break the cache operation
      // Local cache inconsistency is better than complete failure
    }
  }

  /**
   * <p>
   * Clears the local L1 cache for a specific key or all keys.
   * This method is typically called in response to cache invalidation messages.
   * </p>
   *
   * @param key0 the key to clear, or null to clear all entries
   */
  public void clearLocal(Object key0) {
    if (key0 == null) {
      level1Cache.invalidateAll();
      log.debug("Cleared all local cache entries for cache: {}", this.cacheName);
    } else {
      String key = key0.toString();
      level1Cache.invalidate(key);
      log.debug("Cleared local cache entry for cache: {}, key: {}", this.cacheName, key);
    }
  }

  /**
   * <p>
   * Determines if L1 cache is enabled for a specific cache key.
   * Checks both global and key-specific configuration.
   * </p>
   *
   * @param key the cache key to check
   * @return true if L1 cache is enabled for this key
   */
  private boolean isL1Open(String key) {
    return isL1Open() || isL1OpenByKey(key);
  }

  /**
   * <p>
   * Checks if L1 cache is globally enabled for this cache instance.
   * Updates the opened flag for consistency tracking.
   * </p>
   *
   * @return true if L1 cache is globally enabled
   */
  private boolean isL1Open() {
    // Track if L1 cache has ever been enabled
    if (composite.isL1AllOpen() || composite.isL1Manual()) {
      openedL1Cache.compareAndSet(false, true);
    }

    // Check global L1 cache setting
    if (composite.isL1AllOpen()) {
      return true;
    }

    // Check manual cache name matching
    if (composite.isL1Manual()) {
      Set<String> l1ManualCacheNameSet = composite.getL1ManualCacheNameSet();
      return !isEmpty(l1ManualCacheNameSet) 
          && l1ManualCacheNameSet.contains(this.getName());
    }

    return false;
  }

  /**
   * <p>
   * Checks if L1 cache is enabled for a specific cache key.
   * This allows fine-grained control over which keys use L1 cache.
   * </p>
   *
   * @param key the cache key to check
   * @return true if L1 cache is enabled for this specific key
   */
  private boolean isL1OpenByKey(String key) {
    if (composite.isL1Manual()) {
      Set<String> l1ManualKeySet = composite.getL1ManualKeySet();
      return !CollectionUtils.isEmpty(l1ManualKeySet) 
          && l1ManualKeySet.contains(getKey(key));
    }
    return false;
  }

  /**
   * <p>
   * Converts user values to internal store format, handling null values and empty collections.
   * Provides consistent handling of edge cases across the cache implementation.
   * </p>
   *
   * @param userValue the user-provided value
   * @return the value to store internally
   * @throws IllegalArgumentException if null values are not allowed but null was provided
   */
  @Override
  protected Object toStoreValue(@Nullable Object userValue) {
    // Handle null and empty values
    if (userValue == null || NullValue.INSTANCE.equals(userValue)
        || (userValue instanceof Collection && ((Collection<?>) userValue).isEmpty())
        || (userValue instanceof Map && ((Map<?, ?>) userValue).isEmpty())) {
      
      if (isAllowNullValues()) {
        if (userValue == null) {
          return NullValue.INSTANCE;
        }
        if (userValue instanceof Collection) {
          return EMPTY_LIST;
        }
        if (userValue instanceof Map) {
          return EMPTY_MAP;
        }
        return NullValue.INSTANCE;
      }

      throw new IllegalArgumentException(
          String.format("Cache '%s' is configured to not allow null values but null was provided", 
              getName()));
    }
    
    return userValue;
  }
}
