package cloud.xcan.angus.l2cache.spring;

import cloud.xcan.angus.core.cache.CacheManagerClear;
import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.lettucex.util.RedisService;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * <p>
 * A cache manager implementation that manages two-level caches combining Redis and Caffeine.
 * This manager creates and maintains RedisCaffeineCache instances with configurable behavior
 * for both local (L1) and distributed (L2) caching layers.
 * </p>
 * 
 * <p>
 * Key features:
 * - Dynamic cache creation based on configuration
 * - Centralized Caffeine cache configuration
 * - Support for cache-specific configurations
 * - Thread-safe cache instance management
 * - Integration with Spring Cache abstraction
 * </p>
 * 
 * <p>
 * Thread Safety: This class is thread-safe and designed for concurrent access.
 * </p>
 */
@Slf4j
public class RedisCaffeineCacheManager implements CacheManager, CacheManagerClear {

  /**
   * Thread-safe map storing cache instances by name.
   * Uses ConcurrentHashMap to ensure thread safety during cache creation and retrieval.
   */
  private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

  /**
   * Configuration properties for L2 cache behavior and settings.
   */
  private final L2CacheProperties l2CacheProperties;

  /**
   * Redis service instance for L2 cache operations.
   * Shared across all cache instances managed by this manager.
   */
  private final RedisService<Object> l2cacheRedisService;

  /**
   * Predefined cache names that are allowed when dynamic creation is disabled.
   * Used for security and resource management in production environments.
   */
  private final Set<String> cacheNames;

  /**
   * Flag indicating whether dynamic cache creation is enabled.
   * When false, only predefined cache names are allowed.
   */
  private final boolean dynamic;

  /**
   * <p>
   * Constructor for creating a cache manager with the specified configuration.
   * </p>
   *
   * @param l2CacheProperties configuration properties for cache behavior
   * @param l2cacheRedisService Redis service for distributed cache operations
   */
  public RedisCaffeineCacheManager(L2CacheProperties l2CacheProperties,
      RedisService<Object> l2cacheRedisService) {
    super();
    this.l2CacheProperties = Objects.requireNonNull(l2CacheProperties, 
        "L2CacheProperties cannot be null");
    this.l2cacheRedisService = Objects.requireNonNull(l2cacheRedisService, 
        "RedisService cannot be null");
    this.dynamic = l2CacheProperties.isDynamic();
    this.cacheNames = l2CacheProperties.getCacheNames();
    
    log.info("Initialized RedisCaffeineCacheManager with dynamic={}, predefined caches={}", 
        dynamic, cacheNames != null ? cacheNames.size() : 0);
  }

  /**
   * <p>
   * Retrieves or creates a cache instance with the specified name.
   * Implements double-checked locking pattern to ensure thread safety during cache creation.
   * </p>
   *
   * @param name the name of the cache to retrieve or create
   * @return the cache instance, or null if dynamic creation is disabled and name is not predefined
   */
  @Override
  public Cache getCache(String name) {
    if (name == null || name.trim().isEmpty()) {
      log.warn("Attempted to get cache with null or empty name");
      return null;
    }

    // First check: return existing cache if available
    Cache cache = cacheMap.get(name);
    if (cache != null) {
      return cache;
    }

    // Security check: if dynamic creation is disabled, only allow predefined names
    if (!dynamic && (cacheNames == null || !cacheNames.contains(name))) {
      log.warn("Cache creation denied for name '{}' - not in predefined cache names and dynamic creation is disabled", name);
      return null;
    }

    // Create new cache instance with thread safety
    try {
      cache = createCache(name);
      Cache existingCache = cacheMap.putIfAbsent(name, cache);
      
      if (existingCache == null) {
        log.debug("Created new cache instance: {}", name);
        return cache;
      } else {
        log.debug("Cache instance already exists, returning existing: {}", name);
        return existingCache;
      }
    } catch (Exception e) {
      log.error("Failed to create cache instance for name: {}", name, e);
      return null;
    }
  }

  /**
   * <p>
   * Creates a new cache instance with the specified name.
   * Configures both L1 (Caffeine) and L2 (Redis) cache layers.
   * </p>
   *
   * @param name the name of the cache to create
   * @return a new RedisCaffeineCache instance
   */
  private Cache createCache(String name) {
    return new RedisCaffeineCache(name, l2cacheRedisService, createCaffeineCache(), l2CacheProperties);
  }

  /**
   * <p>
   * Creates and configures a Caffeine cache instance based on the provided configuration.
   * Applies various cache policies including expiration, size limits, and refresh settings.
   * </p>
   *
   * @return a configured Caffeine cache instance
   */
  public com.github.benmanes.caffeine.cache.Cache<Object, Object> createCaffeineCache() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
    
    L2CacheProperties.Caffeine caffeineConfig = l2CacheProperties.getCaffeine();
    
    // Configure expiration after access (sliding expiration)
    if (caffeineConfig.getExpireAfterAccess() > 0) {
      cacheBuilder.expireAfterAccess(caffeineConfig.getExpireAfterAccess(), TimeUnit.SECONDS);
      log.debug("Configured Caffeine expireAfterAccess: {} seconds", caffeineConfig.getExpireAfterAccess());
    }
    
    // Configure expiration after write (absolute expiration)
    if (caffeineConfig.getExpireAfterWrite() > 0) {
      cacheBuilder.expireAfterWrite(caffeineConfig.getExpireAfterWrite(), TimeUnit.SECONDS);
      log.debug("Configured Caffeine expireAfterWrite: {} seconds", caffeineConfig.getExpireAfterWrite());
    }
    
    // Configure initial capacity for performance optimization
    if (caffeineConfig.getInitialCapacity() > 0) {
      cacheBuilder.initialCapacity(caffeineConfig.getInitialCapacity());
      log.debug("Configured Caffeine initialCapacity: {}", caffeineConfig.getInitialCapacity());
    }
    
    // Configure maximum size to prevent memory issues
    if (caffeineConfig.getMaximumSize() > 0) {
      cacheBuilder.maximumSize(caffeineConfig.getMaximumSize());
      log.debug("Configured Caffeine maximumSize: {}", caffeineConfig.getMaximumSize());
    }
    
    // Configure refresh after write for background refresh
    if (caffeineConfig.getRefreshAfterWrite() > 0) {
      cacheBuilder.refreshAfterWrite(caffeineConfig.getRefreshAfterWrite(), TimeUnit.SECONDS);
      log.debug("Configured Caffeine refreshAfterWrite: {} seconds", caffeineConfig.getRefreshAfterWrite());
    }
    
    return cacheBuilder.build();
  }

  /**
   * <p>
   * Returns the collection of predefined cache names.
   * This represents the caches that are configured and managed by this manager.
   * </p>
   *
   * @return collection of cache names, may be empty but never null
   */
  @Override
  public Collection<String> getCacheNames() {
    return this.cacheNames != null ? this.cacheNames : Set.of();
  }

  /**
   * <p>
   * Clears local L1 cache for a specific cache and key.
   * This method is typically called in response to cache invalidation messages
   * from other application instances.
   * </p>
   *
   * @param name the name of the cache
   * @param key the specific key to clear, or null to clear all keys
   */
  @Override
  public void clearLocal(String name, Object key) {
    if (name == null || name.trim().isEmpty()) {
      log.warn("Attempted to clear local cache with null or empty name");
      return;
    }

    Cache cache = cacheMap.get(name);
    if (cache instanceof RedisCaffeineCache redisCaffeineCache) {
      try {
        redisCaffeineCache.clearLocal(key);
        log.debug("Cleared local cache for name: {}, key: {}", name, key);
      } catch (Exception e) {
        log.error("Error clearing local cache for name: {}, key: {}", name, key, e);
      }
    } else if (cache != null) {
      log.warn("Cache '{}' is not a RedisCaffeineCache instance, cannot clear local cache", name);
    } else {
      log.debug("Cache '{}' not found, cannot clear local cache", name);
    }
  }

  /**
   * <p>
   * Evicts multiple keys from both L1 and L2 caches for the specified cache name.
   * Provides batch eviction for improved performance when clearing multiple related keys.
   * </p>
   *
   * @param name the name of the cache
   * @param keys collection of keys to evict
   */
  @Override
  public void evict(String name, Collection<Object> keys) {
    if (name == null || name.trim().isEmpty()) {
      log.warn("Attempted to evict from cache with null or empty name");
      return;
    }

    if (keys == null || keys.isEmpty()) {
      log.debug("No keys provided for eviction from cache: {}", name);
      return;
    }

    Cache cache = cacheMap.get(name);
    if (cache instanceof RedisCaffeineCache redisCaffeineCache) {
      try {
        redisCaffeineCache.evict(keys);
        log.debug("Evicted {} keys from cache: {}", keys.size(), name);
      } catch (Exception e) {
        log.error("Error evicting keys from cache: {}", name, e);
      }
    } else if (cache != null) {
      log.warn("Cache '{}' is not a RedisCaffeineCache instance, cannot perform batch eviction", name);
    } else {
      log.debug("Cache '{}' not found, cannot evict keys", name);
    }
  }

  /**
   * <p>
   * Returns the number of currently managed cache instances.
   * Useful for monitoring and debugging purposes.
   * </p>
   *
   * @return the number of active cache instances
   */
  public int getCacheCount() {
    return cacheMap.size();
  }

  /**
   * <p>
   * Checks if a cache with the specified name exists.
   * </p>
   *
   * @param name the cache name to check
   * @return true if the cache exists, false otherwise
   */
  public boolean cacheExists(String name) {
    return name != null && cacheMap.containsKey(name);
  }

  /**
   * <p>
   * Removes a cache instance from management.
   * This method should be used carefully as it can lead to resource leaks
   * if the cache is still being used elsewhere.
   * </p>
   *
   * @param name the name of the cache to remove
   * @return true if the cache was removed, false if it didn't exist
   */
  public boolean removeCache(String name) {
    if (name == null || name.trim().isEmpty()) {
      return false;
    }

    Cache removedCache = cacheMap.remove(name);
    if (removedCache != null) {
      log.info("Removed cache instance: {}", name);
      return true;
    }
    return false;
  }

  /**
   * @deprecated Use {@link #createCaffeineCache()} instead.
   * This method name was not following proper naming conventions.
   */
  @Deprecated(since = "1.0.0", forRemoval = true)
  public com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache() {
    return createCaffeineCache();
  }
}
