package cloud.xcan.angus.l2cache.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.lettucex.util.RedisService;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisCaffeineCacheManagerTest {

  @Mock
  private RedisService<Object> redisService;

  @SuppressWarnings("unchecked")
  private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);

  @BeforeEach
  void setUp() {
    PrincipalContext.set(new Principal().setTenantId(-1L).setOptTenantId(-1L));
    lenient().when(redisService.getRedisTemplate()).thenReturn(redisTemplate);
  }

  @AfterEach
  void tearDown() {
    PrincipalContext.remove();
  }

  private RedisCaffeineCacheManager newManager(L2CacheProperties p) {
    return new RedisCaffeineCacheManager(p, redisService);
  }

  @Test
  void constructorRequiresNonNullArgs() {
    L2CacheProperties p = new L2CacheProperties();
    assertThatThrownBy(() -> new RedisCaffeineCacheManager(null, redisService))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new RedisCaffeineCacheManager(p, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void getCacheRejectsBlankName() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    assertThat(mgr.getCache(null)).isNull();
    assertThat(mgr.getCache("   ")).isNull();
  }

  @Test
  void getCacheDeniedWhenDynamicOffAndNameNotPredefined() {
    L2CacheProperties p = new L2CacheProperties();
    p.setDynamic(false);
    p.setCacheNames(Set.of("allowed"));
    RedisCaffeineCacheManager mgr = newManager(p);
    assertThat(mgr.getCache("other")).isNull();
  }

  @Test
  void getCacheAllowedWhenPredefined() {
    L2CacheProperties p = new L2CacheProperties();
    p.setDynamic(false);
    p.setCacheNames(Set.of("allowed"));
    Cache c = newManager(p).getCache("allowed");
    assertThat(c).isInstanceOf(RedisCaffeineCache.class);
    assertThat(c.getName()).isEqualTo("allowed");
  }

  @Test
  void getCacheReusesInstance() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    Cache a = mgr.getCache("users");
    Cache b = mgr.getCache("users");
    assertThat(a).isSameAs(b);
    assertThat(mgr.getCacheCount()).isEqualTo(1);
  }

  @Test
  void getCacheNamesFallsBackToEmptyWhenNullSet() {
    L2CacheProperties p = new L2CacheProperties();
    p.setCacheNames(null);
    assertThat(newManager(p).getCacheNames()).isEmpty();
  }

  @Test
  void clearLocalNoOpForBlankName() {
    newManager(new L2CacheProperties()).clearLocal("", null);
  }

  @Test
  void clearLocalWhenCacheMissing() {
    newManager(new L2CacheProperties()).clearLocal("missing", "k");
  }

  @Test
  void clearLocalOnExistingCache() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    mgr.getCache("c1");
    mgr.clearLocal("c1", "kk");
  }

  @Test
  void evictNoOpForBlankNameOrEmptyKeys() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    mgr.evict("", List.of("a"));
    mgr.evict("c", Collections.emptyList());
  }

  @Test
  void evictWhenCacheMissing() {
    newManager(new L2CacheProperties()).evict("nope", List.of("k"));
  }

  @Test
  void evictBatchOnRedisCaffeineCache() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    mgr.getCache("batch");
    mgr.evict("batch", List.of("a", "b"));
  }

  @Test
  void removeCacheAndExists() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    mgr.getCache("x");
    assertThat(mgr.cacheExists("x")).isTrue();
    assertThat(mgr.removeCache("x")).isTrue();
    assertThat(mgr.cacheExists("x")).isFalse();
    assertThat(mgr.removeCache(null)).isFalse();
    assertThat(mgr.removeCache("   ")).isFalse();
  }

  @Test
  void createCaffeineCacheAppliesAllPositiveSettings() {
    L2CacheProperties p = new L2CacheProperties();
    p.getCaffeine().setExpireAfterAccess(1);
    p.getCaffeine().setExpireAfterWrite(2);
    p.getCaffeine().setRefreshAfterWrite(3);
    p.getCaffeine().setInitialCapacity(8);
    p.getCaffeine().setMaximumSize(50);
    RedisCaffeineCacheManager mgr = newManager(p);
    assertThat(mgr.createCaffeineCache()).isNotNull();
  }

  @Test
  void deprecatedCaffeineCacheBuildsInstance() {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    assertThat(mgr.caffeineCache()).isNotNull();
  }

  @Test
  void clearLocalWarnsWhenCacheIsNotRedisCaffeine() throws Exception {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    @SuppressWarnings("unchecked")
    ConcurrentMap<String, Cache> map =
        (ConcurrentMap<String, Cache>) readField(mgr, "cacheMap");
    Cache foreign = mock(Cache.class);
    map.put("foreign", foreign);
    mgr.clearLocal("foreign", "k");
    verifyNoInteractions(foreign);
  }

  @Test
  void evictWarnsWhenCacheIsNotRedisCaffeine() throws Exception {
    RedisCaffeineCacheManager mgr = newManager(new L2CacheProperties());
    @SuppressWarnings("unchecked")
    ConcurrentMap<String, Cache> map =
        (ConcurrentMap<String, Cache>) readField(mgr, "cacheMap");
    Cache foreign = mock(Cache.class);
    map.put("foreign", foreign);
    mgr.evict("foreign", List.of("a"));
    verifyNoInteractions(foreign);
  }

  private static Object readField(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }
}
