package cloud.xcan.sdf.l2cache.config;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xcan.l2cache")
public class L2CacheProperties {

  private Boolean enabled = false;

  /**
   * Whether to store null values, When set to true, it can prevent cache penetration.
   */
  private boolean allowNullValues = true;

  /**
   * Whether to dynamically create Cache implementations based on cacheName, Default is true.
   */
  private boolean dynamic = true;

  private Set<String> cacheNames = new HashSet<>();

  private final Composite composite = new Composite();
  private final Caffeine caffeine = new Caffeine();
  private final Redis redis = new Redis();

  public interface Config {
  }

  /**
   * Composite cache configuration
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Composite implements Config {

    /**
     * Whether to enable Level 1 cache globally, Default is false.
     */
    private boolean l1AllOpen = false;

    /**
     * Whether to manually enable Level 1 cache, Default is false.
     */
    private boolean l1Manual = false;

    /**
     * Manually configured set of cache keys that use Level 1 cache, For individual key dimensions.
     */
    private Set<String> l1ManualKeySet = new HashSet<>();

    /**
     * Manually configured set of cache names that use Level 1 cache, For cacheName dimensions.
     */
    private Set<String> l1ManualCacheNameSet = new HashSet<>();
  }

  /**
   * Caffeine specific cache properties.
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Caffeine implements Config {

    /**
     * Expiration time after access, In seconds.
     */
    private long expireAfterAccess;

    /**
     * Expiration time after write, In seconds.
     */
    private long expireAfterWrite;

    /**
     * Refresh time after write, In seconds.
     */
    private long refreshAfterWrite;

    /**
     * Initial size.
     */
    private int initialCapacity;

    /**
     * Maximum number of cached objects, When this number is exceeded, previously cached objects will expire.
     */
    private long maximumSize;
  }

  /**
   * Redis specific cache properties.
   */
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Redis implements Config {

    /**
     * Global expiration time, In milliseconds, default is no expiration.
     */
    private long defaultExpiration = 0;

    /**
     * Penetration expiration time, In milliseconds, default is no expiration.
     */
    private long defaultPenetrationExpiration = 5 * 60 * 1000;

    /**
     * Expiration time for each cacheName, In milliseconds, takes precedence over defaultExpiration.
     */
    private Map<String, Long> expires = new HashMap<>();

    /**
     * Expiration time for cacheName that does not allow penetration, In milliseconds, priority higher than defaultExpiration.
     */
    private Map<String, Long> penetrationExpires = new HashMap<>();

    /**
     * Topic name for notifying other nodes when the cache is updated.
     */
    private String topic = "l2cache:topic";

  }
}
