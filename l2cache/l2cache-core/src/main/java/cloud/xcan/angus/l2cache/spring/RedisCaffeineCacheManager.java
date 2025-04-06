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

@Slf4j
public class RedisCaffeineCacheManager implements CacheManager, CacheManagerClear {

  private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

  private final L2CacheProperties l2CacheProperties;

  private final RedisService<Object> l2cacheRedisService;

  private final Set<String> cacheNames;

  private boolean dynamic = true;

  public RedisCaffeineCacheManager(L2CacheProperties l2CacheProperties,
      RedisService<Object> l2cacheRedisService) {
    super();
    this.l2CacheProperties = l2CacheProperties;
    this.l2cacheRedisService = l2cacheRedisService;
    this.dynamic = l2CacheProperties.isDynamic();
    this.cacheNames = l2CacheProperties.getCacheNames();
  }

  @Override
  public Cache getCache(String name) {
    Cache cache = cacheMap.get(name);
    if (cache != null) {
      return cache;
    }
    if (!dynamic && !cacheNames.contains(name)) {
      return cache;
    }
    cache = new RedisCaffeineCache(name, l2cacheRedisService, caffeineCache(), l2CacheProperties);
    Cache oldCache = cacheMap.putIfAbsent(name, cache);
    log.debug("Create cache instance : {}", name);
    return oldCache == null ? cache : oldCache;
  }

  public com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
    if (l2CacheProperties.getCaffeine().getExpireAfterAccess() > 0) {
      cacheBuilder.expireAfterAccess(l2CacheProperties.getCaffeine().getExpireAfterAccess(),
          TimeUnit.SECONDS);
    }
    if (l2CacheProperties.getCaffeine().getExpireAfterWrite() > 0) {
      cacheBuilder.expireAfterWrite(l2CacheProperties.getCaffeine().getExpireAfterWrite(),
          TimeUnit.SECONDS);
    }
    if (l2CacheProperties.getCaffeine().getInitialCapacity() > 0) {
      cacheBuilder.initialCapacity(l2CacheProperties.getCaffeine().getInitialCapacity());
    }
    if (l2CacheProperties.getCaffeine().getMaximumSize() > 0) {
      cacheBuilder.maximumSize(l2CacheProperties.getCaffeine().getMaximumSize());
    }
    if (l2CacheProperties.getCaffeine().getRefreshAfterWrite() > 0) {
      cacheBuilder.refreshAfterWrite(l2CacheProperties.getCaffeine().getRefreshAfterWrite(),
          TimeUnit.SECONDS);
    }
    return cacheBuilder.build();
  }

  @Override
  public Collection<String> getCacheNames() {
    return this.cacheNames;
  }

  @Override
  public void clearLocal(String name, Object key) {
    Cache cache = cacheMap.get(name);
    if (Objects.nonNull(cache)) {
      RedisCaffeineCache redisCaffeineCache = (RedisCaffeineCache) cache;
      redisCaffeineCache.clearLocal(key);
    }
  }

  @Override
  public void evict(String name, Collection<Object> keys) {
    Cache cache = cacheMap.get(name);
    if (Objects.nonNull(cache)) {
      RedisCaffeineCache redisCaffeineCache = (RedisCaffeineCache) cache;
      redisCaffeineCache.evict(keys);
    }
  }
}
