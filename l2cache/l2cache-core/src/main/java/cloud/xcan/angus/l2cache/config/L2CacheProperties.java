package cloud.xcan.angus.l2cache.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>
 * Configuration properties for the two-level cache (L2Cache) system that combines
 * local Caffeine cache (L1) with distributed Redis cache (L2). This class provides
 * comprehensive configuration options for cache behavior, expiration policies,
 * and synchronization settings.
 * </p>
 * 
 * <p>
 * Key configuration areas:
 * - Global cache enablement and behavior settings
 * - Composite cache configuration for L1/L2 coordination
 * - Caffeine-specific L1 cache properties
 * - Redis-specific L2 cache properties
 * - Cache name and key-based granular control
 * </p>
 * 
 * <p>
 * Configuration example:
 * <pre>
 * xcan:
 *   l2cache:
 *     enabled: true
 *     allow-null-values: true
 *     composite:
 *       l1-all-open: false
 *       l1-manual: true
 *       l1-manual-cache-name-set: [userCache, productCache]
 *     caffeine:
 *       maximum-size: 1000
 *       expire-after-write: 300
 *     redis:
 *       default-expiration: 3600000
 *       topic: "app:cache:topic"
 * </pre>
 * </p>
 * 
 * @see Composite
 * @see Caffeine
 * @see Redis
 */
@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xcan.l2cache")
public class L2CacheProperties {

  /**
   * <p>
   * Global switch to enable or disable the L2Cache system.
   * When disabled, the cache system will not be initialized.
   * </p>
   */
  private Boolean enabled = false;

  /**
   * <p>
   * Whether to store null values in the cache. When set to true, it can prevent
   * cache penetration attacks by caching null results for a short period.
   * </p>
   * 
   * <p>
   * Benefits of allowing null values:
   * - Prevents repeated database queries for non-existent data
   * - Reduces load on downstream systems
   * - Improves overall system performance
   * </p>
   */
  private boolean allowNullValues = true;

  /**
   * <p>
   * Whether to dynamically create Cache implementations based on cacheName.
   * When enabled, caches are created on-demand when first accessed.
   * </p>
   * 
   * <p>
   * Dynamic cache creation provides flexibility but may impact performance
   * for the first access to each cache. Disable for pre-configured cache scenarios.
   * </p>
   */
  private boolean dynamic = true;

  /**
   * <p>
   * Set of predefined cache names that should be initialized at startup.
   * This is useful when dynamic cache creation is disabled or when you want
   * to ensure specific caches are available immediately.
   * </p>
   */
  private Set<String> cacheNames = new HashSet<>();

  /**
   * Composite cache configuration for coordinating L1 and L2 cache behavior.
   */
  private final Composite composite = new Composite();

  /**
   * Caffeine-specific configuration for the local L1 cache.
   */
  private final Caffeine caffeine = new Caffeine();

  /**
   * Redis-specific configuration for the distributed L2 cache.
   */
  private final Redis redis = new Redis();

  /**
   * <p>
   * Marker interface for cache configuration classes.
   * Provides type safety and consistent structure across configuration components.
   * </p>
   */
  public interface Config {
    // Marker interface - no methods required
  }

  /**
   * <p>
   * Configuration for composite cache behavior that coordinates between
   * L1 (Caffeine) and L2 (Redis) caches. This class controls when and how
   * the local cache is used in conjunction with the distributed cache.
   * </p>
   * 
   * <p>
   * Cache levels:
   * - L1: Local Caffeine cache for ultra-fast access
   * - L2: Distributed Redis cache for data persistence and sharing
   * </p>
   * 
   * <p>
   * L1 cache can be enabled globally, manually per cache name, or manually per key.
   * Manual configuration provides fine-grained control over which data uses local caching.
   * </p>
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Composite implements Config {

    /**
     * <p>
     * Whether to enable Level 1 cache globally for all cache operations.
     * When enabled, all cache reads will first check the local Caffeine cache.
     * </p>
     * 
     * <p>
     * Global L1 cache provides maximum performance but may lead to memory
     * usage concerns in high-volume scenarios. Use with appropriate sizing.
     * </p>
     */
    private boolean l1AllOpen = false;

    /**
     * <p>
     * Whether to enable manual Level 1 cache control. When enabled, L1 cache
     * is only used for specifically configured cache names or keys.
     * </p>
     * 
     * <p>
     * Manual control allows selective use of local caching for frequently
     * accessed data while keeping memory usage under control.
     * </p>
     */
    private boolean l1Manual = false;

    /**
     * <p>
     * Set of specific cache keys that should use Level 1 cache.
     * This provides the finest level of granular control over local caching.
     * </p>
     * 
     * <p>
     * Use this for individual keys that are accessed very frequently
     * and would benefit from local caching. Keys should include the full
     * cache key including any prefixes.
     * </p>
     */
    private Set<String> l1ManualKeySet = new HashSet<>();

    /**
     * <p>
     * Set of cache names that should use Level 1 cache.
     * This enables L1 caching for entire cache categories.
     * </p>
     * 
     * <p>
     * Use this for cache categories where most or all keys would benefit
     * from local caching, such as user sessions or frequently accessed
     * reference data.
     * </p>
     */
    private Set<String> l1ManualCacheNameSet = new HashSet<>();
  }

  /**
   * <p>
   * Configuration properties specific to Caffeine local cache (L1).
   * These settings control the behavior, size limits, and expiration
   * policies for the local cache component.
   * </p>
   * 
   * <p>
   * Caffeine provides high-performance local caching with various eviction
   * and expiration strategies. Proper configuration is crucial for balancing
   * performance and memory usage.
   * </p>
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Caffeine implements Config {

    /**
     * <p>
     * Expiration time after last access, in seconds.
     * Entries will be automatically removed if not accessed within this timeframe.
     * </p>
     * 
     * <p>
     * Access-based expiration is useful for data that becomes less relevant
     * over time. Set to 0 to disable access-based expiration.
     * </p>
     */
    private long expireAfterAccess;

    /**
     * <p>
     * Expiration time after write, in seconds.
     * Entries will be automatically removed after this time since creation or last update.
     * </p>
     * 
     * <p>
     * Write-based expiration ensures data freshness by forcing cache misses
     * after a specified time. Useful for data that changes frequently.
     * </p>
     */
    private long expireAfterWrite;

    /**
     * <p>
     * Refresh time after write, in seconds.
     * Triggers asynchronous refresh of cache entries after this time.
     * </p>
     * 
     * <p>
     * Refresh allows serving stale data while loading fresh data in the background,
     * providing better user experience than cache misses.
     * </p>
     */
    private long refreshAfterWrite;

    /**
     * <p>
     * Initial capacity for the cache.
     * Setting this appropriately can improve performance by reducing resizing operations.
     * </p>
     * 
     * <p>
     * Choose a value based on expected cache size to minimize memory allocations
     * and improve startup performance.
     * </p>
     */
    private int initialCapacity;

    /**
     * <p>
     * Maximum number of cached objects.
     * When this limit is exceeded, least recently used entries will be evicted.
     * </p>
     * 
     * <p>
     * This is a crucial setting for memory management. Monitor memory usage
     * and adjust based on available heap space and performance requirements.
     * </p>
     */
    private long maximumSize;
  }

  /**
   * <p>
   * Configuration properties specific to Redis distributed cache (L2).
   * These settings control expiration policies, cache penetration protection,
   * and inter-node synchronization for the distributed cache component.
   * </p>
   * 
   * <p>
   * Redis serves as the authoritative cache layer that ensures data consistency
   * across multiple application instances and provides persistence.
   * </p>
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Redis implements Config {

    /**
     * <p>
     * Global default expiration time for cache entries, in milliseconds.
     * Applied to all cache entries unless overridden by cache-specific settings.
     * </p>
     * 
     * <p>
     * Set to 0 for no expiration (persistent cache). Consider memory usage
     * and data freshness requirements when setting this value.
     * </p>
     */
    private long defaultExpiration = 0;

    /**
     * <p>
     * Default expiration time for cache penetration protection, in milliseconds.
     * Applied to null/empty values to prevent repeated database queries.
     * </p>
     * 
     * <p>
     * Penetration protection caches negative results for a shorter period
     * to balance protection against cache pollution. Default is 5 minutes.
     * </p>
     */
    private long defaultPenetrationExpiration = 5 * 60 * 1000;

    /**
     * <p>
     * Cache-specific expiration times mapped by cache name, in milliseconds.
     * These settings take precedence over the default expiration time.
     * </p>
     * 
     * <p>
     * Use this for caches with different data lifecycle requirements.
     * For example, user sessions might have longer expiration than temporary data.
     * </p>
     */
    private Map<String, Long> expires = new HashMap<>();

    /**
     * <p>
     * Cache-specific penetration protection expiration times mapped by cache name.
     * These settings take precedence over the default penetration expiration.
     * </p>
     * 
     * <p>
     * Allows fine-tuning of penetration protection based on the nature of
     * different cache types and their susceptibility to penetration attacks.
     * </p>
     */
    private Map<String, Long> penetrationExpires = new HashMap<>();

    /**
     * <p>
     * Redis pub/sub topic name for cache invalidation notifications.
     * Used to synchronize cache updates across multiple application instances.
     * </p>
     * 
     * <p>
     * Ensure this topic name is unique to your application to avoid
     * conflicts with other applications using the same Redis instance.
     * </p>
     */
    private String topic = "l2cache:topic";
  }
}
