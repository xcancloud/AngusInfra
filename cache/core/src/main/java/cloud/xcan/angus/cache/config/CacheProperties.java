package cloud.xcan.angus.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cache configuration properties.
 */
@Data
@ConfigurationProperties(prefix = "angus.cache")
public class CacheProperties {

  /**
   * Memory cache configuration.
   */
  private MemoryCacheProperties memory = new MemoryCacheProperties();

  /**
   * Management REST API configuration.
   */
  private ManagementProperties management = new ManagementProperties();

  @Data
  public static class MemoryCacheProperties {

    /**
     * Maximum number of entries in memory cache.
     */
    private long maxSize = 10000;

    /**
     * Retained for API compatibility; no longer drives Caffeine eviction. Caffeine uses per-entry
     * TTL derived from each entry's expireAt timestamp.
     */
    private long cleanupIntervalSeconds = 300;
  }

  @Data
  public static class ManagementProperties {

    /**
     * Whether to expose the cache management REST API under {@code /api/v1/cache}. Disabled by
     * default — enable only in trusted / internal environments and always protect the endpoints
     * with authentication (e.g. Spring Security).
     */
    private boolean enabled = false;
  }
}
