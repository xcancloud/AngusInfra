package cloud.xcan.angus.cache;

import java.util.Optional;

/**
 * Distributed Cache Interface Defines all cache operations
 */
public interface IDistributedCache {

  /**
   * Set a cache value with optional TTL
   *
   * @param key        Cache key
   * @param value      Cache value
   * @param ttlSeconds TTL in seconds, null means no expiration
   */
  void set(String key, String value, Long ttlSeconds);

  /**
   * Set a cache value without expiration
   *
   * @param key   Cache key
   * @param value Cache value
   */
  void set(String key, String value);

  /**
   * Get a cache value
   *
   * @param key Cache key
   * @return Optional containing the value if exists and not expired
   */
  Optional<String> get(String key);

  /**
   * Delete a cache entry
   *
   * @param key Cache key
   * @return true if deleted, false if not found
   */
  boolean delete(String key);

  /**
   * Check if key exists and is not expired
   *
   * @param key Cache key
   * @return true if exists and not expired
   */
  boolean exists(String key);

  /**
   * Get TTL for a key
   *
   * @param key Cache key
   * @return TTL in seconds, -1 if no expiration, -2 if key not found
   */
  long getTTL(String key);

  /**
   * Set expiration for an existing key
   *
   * @param key        Cache key
   * @param ttlSeconds TTL in seconds
   * @return true if successful, false if key not found
   */
  boolean expire(String key, long ttlSeconds);

  /**
   * Clear all cache entries
   */
  void clear();

  /**
   * Get cache statistics
   *
   * @return CacheStats object
   */
  CacheStats getStats();

  /**
   * Cleanup expired entries from persistence and return number deleted
   */
  int cleanupExpiredEntries();
}
