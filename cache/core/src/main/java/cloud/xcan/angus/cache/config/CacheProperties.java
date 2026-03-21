package cloud.xcan.angus.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cache configuration properties
 */
@Data
@ConfigurationProperties(prefix = "angus.cache")
public class CacheProperties {

  /**
   * Memory cache configuration
   */
  private MemoryCacheProperties memory = new MemoryCacheProperties();

  @Data
  public static class MemoryCacheProperties {

    /**
     * Maximum number of entries in memory cache
     */
    private long maxSize = 10000;

    /**
     * Cleanup interval in seconds for expired entries
     */
    private long cleanupIntervalSeconds = 300;
  }
}
