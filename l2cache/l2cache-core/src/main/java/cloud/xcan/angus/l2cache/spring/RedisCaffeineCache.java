package cloud.xcan.angus.l2cache.spring;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.l2cache.synchronous.CacheMessage;
import cloud.xcan.angus.lettucex.util.RedisService;
import cloud.xcan.angus.spec.utils.ObjectUtils;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

@Slf4j
public class RedisCaffeineCache extends AbstractValueAdaptingCache {

  private String cacheName;

  /**
   * Level 1 cache.
   */
  private Cache<Object, Object> level1Cache;

  /**
   * Level 2 cache instance.
   */
  private RedisService<Object> redisService;

  private long defaultExpiration = 0;

  private long defaultPenetrationExpiration = 5 * 60 * 1000;

  private final static String cachePrefix = "j2cache:";

  private Map<String, Long> expires;

  private Map<String, Long> penetrationExpires;

  private L2CacheProperties.Composite composite;

  private L2CacheProperties.Redis redis;

  /**
   * Records whether Level 1 cache has been enabled. Once enabled, it is set to true.
   * <p>
   * The following scenario may cause inconsistency between local cache and Redis cache: Enable
   * local cache, update user data, disable local cache, update user information in Redis, and then
   * enable local cache again. Solution: In the case of put or evict, if the Level 1 cache switch in
   * the configuration center is turned off and the local Level 1 cache switch is turned on, clear
   * the Level 1 cache.
   */
  private final AtomicBoolean openedL1Cache = new AtomicBoolean();

  private final Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<>();

  protected RedisCaffeineCache(boolean allowNullValues) {
    super(allowNullValues);
  }

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

  @Override
  public <T> T get(Object key0, Callable<T> call) {
    String key = key0.toString();

    Object value = lookup(key);
    if (value != null) {
      return (T) value;
    }

    ReentrantLock lock = keyLockMap.get(key.toString());
    if (lock == null) {
      log.debug("Create lock for key : {}", key);
      lock = new ReentrantLock();
      keyLockMap.putIfAbsent(key.toString(), lock);
    }
    try {
      lock.lock();
      value = lookup(key);
      if (value != null) {
        return (T) value;
      }
      value = call.call();
      Object storeValue = toStoreValue(value);
      put(key, storeValue);
      return (T) value;
    } catch (Exception e) {
      throw new ValueRetrievalException(key, call, e.getCause());
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void put(Object key0, Object value) {
    String key = key0.toString();

    // If the value cannot be null but the actual value is null, clear the data.
    // if (!super.isAllowNullValues() && isEmptyValue(storeValue)) {
    //  this.evict(key);
    //  return;
    // }

    // Prevent cache penetration when the query result is empty.
    Object storeValue = toStoreValue(value);
    if (isEmptyValue(storeValue)) {
      emptyPenetrationSafe(key, storeValue);
      return;
    }

    // If null values are allowed.
    String cacheKey = getKey(key);
    if (isEmptyValue(value)) {
      return;
    }

    long expire = getExpire();
    if (expire > 0) {
      redisService.set(cacheKey, storeValue, expire, TimeUnit.MILLISECONDS);
    } else {
      redisService.set(cacheKey, storeValue);
    }

    // Check if Level 1 cache is enabled.
    boolean isL1Open = isL1Open(cacheKey);
    if (isL1Open) {
      // Notify all nodes to clear the local cache (including the local node).
      clearAllLocalCache(CacheMessage.of(this.cacheName, key));
      // Cache to the local Level 1 cache.
      level1Cache.put(key, toStoreValue(value));
    }
  }

  public void emptyPenetrationSafe(String key, Object storeValue) {
    // Must meet the conditions: toStoreValue(value) and isEmptyValue(storeValue)

    // If the value cannot be null but the actual value is null, clear the data.
    if (!super.isAllowNullValues()) {
      this.evict(key);
      return;
    }

    String cacheKey = getKey(key);
    // Object storeValue = toStoreValue(storeValue);
    if (penetrationExpires.containsKey(cacheKey)) {
      long expire = getPenetrationExpire();
      // Cache an empty value (not null) to prevent cache penetration. For lookup(key), only queries with null values will penetrate.
      redisService.set(cacheKey, storeValue, expire, TimeUnit.MILLISECONDS);
    }
  }

  private boolean isEmptyValue(Object value) {
    return value == null || "null".equals(value) || "".equals(value)
        || NullValue.INSTANCE.equals(value)
        || (value instanceof Collection && ((Collection<?>) value).isEmpty())
        || (value instanceof Map && ((Map<?, ?>) value).isEmpty());
  }

  /**
   * Adds a key-value pair using the putIfAbsent method. If the key does not exist in the map, it is
   * added, and null is returned. If the key already exists, the original value is retained.
   */
  @Override
  public ValueWrapper putIfAbsent(Object key0, Object value) {
    String key = key0.toString();

    String cacheKey = getKey(key);
    Object prevValue = null;
    // Consider using a distributed lock or making Redis's setIfAbsent atomic.
    synchronized (key) {
      prevValue = redisService.get(cacheKey);
      if (prevValue == null) {
        long expire = getExpire();
        if (expire > 0) {
          redisService.set(getKey(key), toStoreValue(value), expire, TimeUnit.MILLISECONDS);
        } else {
          redisService.set(getKey(key), toStoreValue(value));
        }

        clearAllLocalCache(CacheMessage.of(this.cacheName, key));

        level1Cache.put(key, toStoreValue(value));
      }
    }
    return toValueWrapper(prevValue);
  }

  @Override
  public void evict(Object key0) {
    String key = key0.toString();
    // First, clear the cache data in Redis, then clear the cache in Caffeine to avoid other requests reloading data from Redis into Caffeine shortly after clearing the Caffeine cache.
    redisService.delete(getKey(key));

    clearAllLocalCache(CacheMessage.of(this.cacheName, key));

    level1Cache.invalidate(key);
  }

  public void evict(Collection<Object> keys) {
    if (ObjectUtils.isNotEmpty(keys)) {
      for (Object key : keys) {
        evict(key.toString());
      }
    }
  }

  @Override
  public void clear() {
    // First, clear the cache data in Redis, then clear the cache in Caffeine to avoid other requests reloading data from Redis into Caffeine shortly after clearing the Caffeine cache.
    Set<String> keys = redisService.keys(this.cacheName.concat(":*"));
    for (String key : keys) {
      redisService.delete(key);
    }
    clearAllLocalCache(CacheMessage.of(this.cacheName, null));
    level1Cache.invalidateAll();
  }

  @Override
  public Object lookup(Object key0) {
    String key = key0.toString();
    String cacheKey = getKey(key);

    // Check if Level 1 cache is enabled.
    Object value;
    boolean isL1Open = isL1Open(cacheKey);
    if (isL1Open) {
      // Get the cache from Level 1.
      value = level1Cache.getIfPresent(key);
      if (value != null) {
        if (log.isDebugEnabled()) {
          log.debug("Get level1 cache, name={}, key={}, value={}", this.getName(), key, value);
        }
        return value;
      }
    }

    // Get the cache from Level 2.
    value = redisService.get(cacheKey);
    if (value != null && isL1Open) {
      if (log.isDebugEnabled()) {
        log.debug("Get level2 cache and put in level1, name={}, key={}, value={}",
            this.getName(), key, value);
      }
      level1Cache.put(key, toStoreValue(value));
    }
    return value;
  }

  private String getKey(String key) {
    return cachePrefix.concat(cacheName)
        .concat(":").concat(getOptTenantId().toString())
        .concat(":").concat(key);
  }

  private long getExpire() {
    Long cacheNameExpire = expires.get(this.cacheName);
    return cacheNameExpire == null ? defaultExpiration : cacheNameExpire;
  }

  private long getPenetrationExpire() {
    Long cacheNameExpire = penetrationExpires.get(this.cacheName);
    return cacheNameExpire == null ? defaultPenetrationExpiration : cacheNameExpire;
  }

  /**
   * Notify other nodes to clear their local cache when the cache changes.
   */
  private void clearAllLocalCache(CacheMessage message) {
    redisService.getRedisTemplate().convertAndSend(redis.getTopic(), message);
  }

  /**
   * Clear the local cache.
   */
  public void clearLocal(Object key0) {
    String key = key0.toString();
    log.debug("Clear local cache, the key is : {}", key);
    if (key == null) {
      level1Cache.invalidateAll();
    } else {
      level1Cache.invalidate(key);
    }
  }

  /**
   * Check if Level 1 cache is enabled.
   */
  private boolean isL1Open(String key) {
    // Check the switch and cache name.
    if (isL1Open()) {
      return true;
    }
    // Check the key.
    return ifL1OpenByKey(key);
  }

  /**
   * Local cache check: check the switch and cache name.
   */
  private boolean isL1Open() {
    // Check if local cache has been enabled.
    if (composite.isL1AllOpen() || composite.isL1Manual()) {
      openedL1Cache.compareAndSet(false, true);
    }
    // Check if Level 1 cache is enabled.
    if (composite.isL1AllOpen()) {
      return true;
    }
    // Check if manual matching is enabled.
    if (composite.isL1Manual()) {
      // Manually match the cache name set (for cacheName dimension).
      Set<String> l1ManualCacheNameSet = composite.getL1ManualCacheNameSet();
      return !isEmpty(l1ManualCacheNameSet)
          && composite.getL1ManualCacheNameSet().contains(this.getName());
    }
    return false;
  }

  /**
   * Local cache check: check the key.
   */
  private boolean ifL1OpenByKey(String key) {
    // Check if manual matching is enabled.
    if (composite.isL1Manual()) {
      // Manually match the cache key set (for individual key dimension).
      Set<String> l1ManualKeySet = composite.getL1ManualKeySet();
      return !CollectionUtils.isEmpty(l1ManualKeySet) && l1ManualKeySet.contains(getKey(key));
    }
    return false;
  }

  /**
   * Convert the given user value, as passed into the put method, to a value in the internal store
   * (adapting {@code null}).
   *
   * @param userValue the given user value
   * @return the value to store
   */
  @Override
  @NotNull
  protected Object toStoreValue(@Nullable Object userValue) {
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

      throw new IllegalArgumentException("Cache '" + getName()
          + "' is configured to not allow null values but null was provided");
    }
    return userValue;
  }
}
