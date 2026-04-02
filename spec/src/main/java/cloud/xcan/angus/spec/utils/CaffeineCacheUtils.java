package cloud.xcan.angus.spec.utils;


import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Caffeine缓存工具类 提供统一的缓存管理、多种缓存策略和便捷的操作方法
 */
@Slf4j
public class CaffeineCacheUtils {

  // 默认缓存配置
  private static final long DEFAULT_MAXIMUM_SIZE = 2048;
  private static final long DEFAULT_EXPIRE_AFTER_WRITE_MINUTES = 30;
  private static final long DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES = 10;

  // 缓存管理器（单例）
  private static final Map<String, Cache<Object, Object>> CACHE_MAP = new ConcurrentHashMap<>();
  private static final Map<String, AsyncCache<Object, Object>> ASYNC_CACHE_MAP = new ConcurrentHashMap<>();

  private CaffeineCacheUtils() {
    // 私有构造器，防止实例化
  }

  // ==================== 同步缓存操作 ====================

  /**
   * 创建同步缓存
   */
  public static <K, V> Cache<K, V> createCache(String cacheName) {
    return createCache(cacheName, DEFAULT_MAXIMUM_SIZE);
  }

  /**
   * 创建同步缓存 - 指定最大容量
   */
  public static <K, V> Cache<K, V> createCache(String cacheName, long maximumSize) {
    return createCache(cacheName, maximumSize,
        DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES);
  }

  /**
   * 创建同步缓存 - 完整参数
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Cache<K, V> createCache(String cacheName, long maximumSize,
      long expireAfterWriteMinutes, long expireAfterAccessMinutes) {
    return (Cache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      Caffeine<Object, Object> builder = Caffeine.newBuilder()
          .maximumSize(maximumSize)
          .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
          .expireAfterAccess(expireAfterAccessMinutes, TimeUnit.MINUTES)
          .removalListener((RemovalListener<Object, Object>) (key, value, cause) -> {
            log.debug("Cache [{}] entity removed: key={}, cause={}", cacheName, key, cause);
          });

      // 启用统计（生产环境建议按需开启）
      if (log.isDebugEnabled()) {
        builder.recordStats();
      }

      return builder.build();
    });
  }

  /**
   * 创建带刷新功能的缓存
   */
  @SuppressWarnings("unchecked")
  public static <K, V> LoadingCache<K, V> createRefreshCache(String cacheName,
      long refreshAfterWriteMinutes,
      Function<K, V> loadFunction) {
    return (LoadingCache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      return Caffeine.newBuilder()
          .maximumSize(DEFAULT_MAXIMUM_SIZE)
          .refreshAfterWrite(refreshAfterWriteMinutes, TimeUnit.MINUTES)
          .expireAfterWrite(refreshAfterWriteMinutes * 3, TimeUnit.MINUTES)
          .removalListener((RemovalListener<Object, Object>) (key, value, cause) -> {
            log.debug("Refresh cache [{}] entity removed: key={}, cause={}", cacheName, key, cause);
          })
          .build(new CacheLoader<Object, Object>() {
            @Override
            public Object load(Object key) {
              return loadFunction.apply((K) key);
            }
          });
    });
  }

  /**
   * 创建软引用缓存（内存不足时自动回收）
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Cache<K, V> createSoftValuesCache(String cacheName) {
    return (Cache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      return Caffeine.newBuilder()
          .softValues()
          .maximumSize(DEFAULT_MAXIMUM_SIZE)
          .expireAfterWrite(DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
          .removalListener((RemovalListener<Object, Object>) (key, value, cause) -> {
            log.debug("Soft reference cache [{}] entity removed: key={}, cause={}", cacheName, key,
                cause);
          })
          .build();
    });
  }

  /**
   * 创建弱引用缓存（GC时自动回收）
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Cache<K, V> createWeakValuesCache(String cacheName) {
    return (Cache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      return Caffeine.newBuilder()
          .weakValues()
          .maximumSize(DEFAULT_MAXIMUM_SIZE)
          .expireAfterWrite(DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
          .removalListener((RemovalListener<Object, Object>) (key, value, cause) -> {
            log.debug("Weak reference cache [{}] entity removed: key={}, cause={}", cacheName, key,
                cause);
          })
          .build();
    });
  }

  /**
   * 获取缓存值，如果不存在则计算并存入
   */
  public static <K, V> V get(String cacheName, K key, Function<K, V> loadFunction) {
    Cache<K, V> cache = getCache(cacheName);
    return cache.get(key, loadFunction);
  }

  /**
   * 获取缓存值，如果不存在则使用Supplier计算并存入
   */
  public static <K, V> V get(String cacheName, K key, Supplier<V> supplier) {
    Cache<K, V> cache = getCache(cacheName);
    return cache.get(key, k -> supplier.get());
  }

  /**
   * 获取缓存值，不存在则返回null
   */
  public static <K, V> V getIfPresent(String cacheName, K key) {
    Cache<K, V> cache = getCache(cacheName);
    return cache.getIfPresent(key);
  }

  /**
   * 批量获取缓存值
   */
  public static <K, V> Map<K, V> getAllPresent(String cacheName, Iterable<K> keys) {
    Cache<K, V> cache = getCache(cacheName);
    return cache.getAllPresent(keys);
  }

  /**
   * 放入缓存
   */
  public static <K, V> void put(String cacheName, K key, V value) {
    Cache<K, V> cache = getCache(cacheName);
    cache.put(key, value);
  }

  /**
   * 批量放入缓存
   */
  public static <K, V> void putAll(String cacheName, Map<? extends K, ? extends V> map) {
    Cache<K, V> cache = getCache(cacheName);
    cache.putAll(map);
  }

  /**
   * 使缓存失效
   */
  public static <K> void invalidate(String cacheName, K key) {
    Cache<K, ?> cache = getCache(cacheName);
    cache.invalidate(key);
  }

  /**
   * 批量使缓存失效
   */
  public static <K> void invalidateAll(String cacheName, Iterable<K> keys) {
    Cache<K, ?> cache = getCache(cacheName);
    cache.invalidateAll(keys);
  }

  /**
   * 清空缓存
   */
  public static void clear(String cacheName) {
    Cache<?, ?> cache = (Cache<?, ?>) CACHE_MAP.get(cacheName);
    if (cache != null) {
      cache.invalidateAll();
      log.info("Cache [{}] has been cleared", cacheName);
    }
  }

  /**
   * 获取缓存估计大小
   */
  public static long estimatedSize(String cacheName) {
    Cache<?, ?> cache = (Cache<?, ?>) CACHE_MAP.get(cacheName);
    return cache != null ? cache.estimatedSize() : 0;
  }

  /**
   * 获取缓存统计信息
   */
  public static CacheStats stats(String cacheName) {
    Cache<?, ?> cache = (Cache<?, ?>) CACHE_MAP.get(cacheName);
    return cache != null ? cache.stats() : null;
  }

  /**
   * 打印缓存统计信息
   */
  public static void printStats(String cacheName) {
    CacheStats stats = stats(cacheName);
    if (stats != null) {
      log.info("Cache [{}] stats: hitCount={}, missCount={}, loadSuccessCount={}, " +
              "loadFailureCount={}, totalLoadTime={}, evictionCount={}",
          cacheName, stats.hitCount(), stats.missCount(), stats.loadSuccessCount(),
          stats.loadFailureCount(), stats.totalLoadTime(), stats.evictionCount());
    }
  }

  // ==================== 异步缓存操作 ====================

  /**
   * 创建异步缓存
   */
  public static <K, V> AsyncCache<K, V> createAsyncCache(String cacheName) {
    return createAsyncCache(cacheName, DEFAULT_MAXIMUM_SIZE);
  }

  /**
   * 创建异步缓存 - 指定最大容量
   */
  public static <K, V> AsyncCache<K, V> createAsyncCache(String cacheName, long maximumSize) {
    return createAsyncCache(cacheName, maximumSize,
        DEFAULT_EXPIRE_AFTER_WRITE_MINUTES, DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES);
  }

  /**
   * 创建异步缓存 - 完整参数
   */
  @SuppressWarnings("unchecked")
  public static <K, V> AsyncCache<K, V> createAsyncCache(String cacheName, long maximumSize,
      long expireAfterWriteMinutes,
      long expireAfterAccessMinutes) {
    return (AsyncCache<K, V>) ASYNC_CACHE_MAP.computeIfAbsent(cacheName, name -> {
      Caffeine<Object, Object> builder = Caffeine.newBuilder()
          .maximumSize(maximumSize)
          .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
          .expireAfterAccess(expireAfterAccessMinutes, TimeUnit.MINUTES);

      return builder.buildAsync();
    });
  }

  /**
   * 异步获取缓存值
   */
  public static <K, V> CompletableFuture<V> getAsync(String cacheName, K key,
      Function<K, V> loadFunction) {
    AsyncCache<K, V> cache = getAsyncCache(cacheName);
    return cache.get(key, loadFunction);
  }

  /**
   * 异步获取缓存值，如果不存在则使用Supplier计算
   */
  public static <K, V> CompletableFuture<V> getAsync(String cacheName, K key,
      Supplier<V> supplier) {
    AsyncCache<K, V> cache = getAsyncCache(cacheName);
    return cache.get(key, k -> supplier.get());
  }

  /**
   * 异步获取缓存值，如果不存在则返回null的CompletableFuture
   */
  public static <K, V> CompletableFuture<V> getAsyncIfPresent(String cacheName, K key) {
    AsyncCache<K, V> cache = getAsyncCache(cacheName);
    return cache.getIfPresent(key);
  }

  /**
   * 异步放入缓存
   */
  public static <K, V> void putAsync(String cacheName, K key, V value) {
    AsyncCache<K, V> cache = getAsyncCache(cacheName);
    cache.put(key, CompletableFuture.completedFuture(value));
  }

  /**
   * 异步放入缓存（CompletableFuture形式）
   */
  public static <K, V> void putAsync(String cacheName, K key, CompletableFuture<V> future) {
    AsyncCache<K, V> cache = getAsyncCache(cacheName);
    cache.put(key, future);
  }

  // ==================== 高级功能 ====================

  /**
   * 创建定时过期的缓存（指定过期时间）
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Cache<K, V> createExpireAfterWriteCache(String cacheName,
      Duration duration) {
    return (Cache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      return Caffeine.newBuilder()
          .maximumSize(DEFAULT_MAXIMUM_SIZE)
          .expireAfterWrite(duration)
          .removalListener((RemovalListener<Object, Object>) (key, value, cause) -> {
            log.debug("Timing clear cache [{}] removed: key={}, cause={}", cacheName, key, cause);
          })
          .build();
    });
  }

  /**
   * 创建写入后刷新缓存
   */
  @SuppressWarnings("unchecked")
  public static <K, V> LoadingCache<K, V> createWriteRefreshCache(String cacheName,
      Duration refreshDuration,
      Function<K, V> loadFunction) {
    return (LoadingCache<K, V>) CACHE_MAP.computeIfAbsent(cacheName, name -> {
      return Caffeine.newBuilder()
          .maximumSize(DEFAULT_MAXIMUM_SIZE)
          .refreshAfterWrite(refreshDuration)
          .expireAfterWrite(refreshDuration.multipliedBy(3))
          .build(new CacheLoader<Object, Object>() {
            @Override
            public Object load(Object key) {
              return loadFunction.apply((K) key);
            }

            @Override
            public Object reload(Object key, Object oldValue) {
              log.debug("Cache [{}] reload: key={}", cacheName, key);
              return loadFunction.apply((K) key);
            }
          });
    });
  }

  /**
   * 清除所有缓存
   */
  public static void clearAll() {
    CACHE_MAP.values().forEach(Cache::invalidateAll);
    ASYNC_CACHE_MAP.values().forEach(c -> c.synchronous().invalidateAll());
  }

  /**
   * 获取所有缓存名称
   */
  public static Set<String> getAllCacheNames() {
    return new HashSet<>(CACHE_MAP.keySet());
  }

  /**
   * 销毁指定缓存
   */
  public static void destroyCache(String cacheName) {
    Cache<?, ?> cache = CACHE_MAP.remove(cacheName);
    if (cache != null) {
      cache.invalidateAll();
    }
    AsyncCache<?, ?> asyncCache = ASYNC_CACHE_MAP.remove(cacheName);
    if (asyncCache != null) {
      asyncCache.synchronous().invalidateAll();
    }
  }

  /**
   * 预热缓存
   */
  public static <K, V> void warmUp(String cacheName, Map<K, V> warmUpData) {
    if (warmUpData != null && !warmUpData.isEmpty()) {
      Cache<K, V> cache = getCache(cacheName);
      cache.putAll(warmUpData);
    }
  }

  /**
   * 批量获取或计算（如果不存在）
   */
  public static <K, V> Map<K, V> getAll(String cacheName, Iterable<K> keys,
      Function<Iterable<? extends K>, Map<K, V>> loadFunction) {
    Cache<Object, Object> existing = CACHE_MAP.get(cacheName);
    if (existing instanceof LoadingCache) {
      @SuppressWarnings("unchecked")
      LoadingCache<K, V> loading = (LoadingCache<K, V>) existing;
      return loading.getAll(keys);
    }
    Cache<K, V> cache = getCache(cacheName);
    return cache.getAll(keys, missing -> loadFunction.apply(missing));
  }

  // ==================== 内部方法 ====================

  @SuppressWarnings("unchecked")
  private static <K, V> Cache<K, V> getCache(String cacheName) {
    Cache<K, V> cache = (Cache<K, V>) CACHE_MAP.get(cacheName);
    if (cache == null) {
      // 默认创建一个缓存
      cache = createCache(cacheName);
    }
    return cache;
  }

  @SuppressWarnings("unchecked")
  private static <K, V> AsyncCache<K, V> getAsyncCache(String cacheName) {
    AsyncCache<K, V> cache = (AsyncCache<K, V>) ASYNC_CACHE_MAP.get(cacheName);
    if (cache == null) {
      // 默认创建一个异步缓存
      cache = createAsyncCache(cacheName);
    }
    return cache;
  }

  // ==================== 构建器模式（可选） ====================

  /**
   * Caffeine缓存构建器（链式调用）
   */
  public static class CacheBuilder<K, V> {

    private final String cacheName;
    private long maximumSize = DEFAULT_MAXIMUM_SIZE;
    private Duration expireAfterWrite = Duration.ofMinutes(DEFAULT_EXPIRE_AFTER_WRITE_MINUTES);
    private Duration expireAfterAccess = Duration.ofMinutes(DEFAULT_EXPIRE_AFTER_ACCESS_MINUTES);
    private Duration refreshAfterWrite;
    private boolean weakValues = false;
    private boolean softValues = false;
    private boolean recordStats = false;
    private RemovalListener<K, V> removalListener;
    private Executor executor;

    public CacheBuilder(String cacheName) {
      this.cacheName = cacheName;
    }

    public CacheBuilder<K, V> maximumSize(long maximumSize) {
      this.maximumSize = maximumSize;
      return this;
    }

    public CacheBuilder<K, V> expireAfterWrite(Duration duration) {
      this.expireAfterWrite = duration;
      return this;
    }

    public CacheBuilder<K, V> expireAfterAccess(Duration duration) {
      this.expireAfterAccess = duration;
      return this;
    }

    public CacheBuilder<K, V> refreshAfterWrite(Duration duration) {
      this.refreshAfterWrite = duration;
      return this;
    }

    public CacheBuilder<K, V> weakValues() {
      this.weakValues = true;
      return this;
    }

    public CacheBuilder<K, V> softValues() {
      this.softValues = true;
      return this;
    }

    public CacheBuilder<K, V> recordStats() {
      this.recordStats = true;
      return this;
    }

    public CacheBuilder<K, V> removalListener(RemovalListener<K, V> removalListener) {
      this.removalListener = removalListener;
      return this;
    }

    public CacheBuilder<K, V> executor(Executor executor) {
      this.executor = executor;
      return this;
    }

    /**
     * 构建同步缓存
     */
    public Cache<K, V> build() {
      Caffeine<Object, Object> builder = Caffeine.newBuilder()
          .maximumSize(maximumSize)
          .expireAfterWrite(expireAfterWrite)
          .expireAfterAccess(expireAfterAccess);

      if (refreshAfterWrite != null) {
        builder.refreshAfterWrite(refreshAfterWrite);
      }

      if (weakValues) {
        builder.weakValues();
      }

      if (softValues) {
        builder.softValues();
      }

      if (recordStats) {
        builder.recordStats();
      }

      if (removalListener != null) {
        builder.removalListener(removalListener);
      } else {
        builder.removalListener((key, value, cause) -> {
          log.debug("Cache [{}] removed: key={}, cause={}", cacheName, key, cause);
        });
      }

      @SuppressWarnings("unchecked")
      Cache<K, V> cache = (Cache<K, V>) CACHE_MAP.computeIfAbsent(cacheName,
          name -> builder.build());

      return cache;
    }

    /**
     * 构建异步缓存
     */
    public AsyncCache<K, V> buildAsync() {
      Caffeine<Object, Object> builder = Caffeine.newBuilder()
          .maximumSize(maximumSize)
          .expireAfterWrite(expireAfterWrite)
          .expireAfterAccess(expireAfterAccess);

      if (refreshAfterWrite != null) {
        builder.refreshAfterWrite(refreshAfterWrite);
      }

      if (weakValues) {
        builder.weakValues();
      }

      if (softValues) {
        builder.softValues();
      }

      if (recordStats) {
        builder.recordStats();
      }

      if (removalListener != null) {
        builder.removalListener(removalListener);
      }

      if (executor != null) {
        builder.executor(executor);
      }

      @SuppressWarnings("unchecked")
      AsyncCache<K, V> cache = (AsyncCache<K, V>) ASYNC_CACHE_MAP.computeIfAbsent(cacheName,
          name -> builder.buildAsync());

      return cache;
    }
  }

  /**
   * 创建缓存构建器
   */
  public static <K, V> CacheBuilder<K, V> builder(String cacheName) {
    return new CacheBuilder<>(cacheName);
  }
}
