package cloud.xcan.angus.l2cache.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.lettucex.util.RedisService;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisCaffeineCacheTest {

  private static final String CACHE_NAME = "demo";

  @Mock
  private RedisService<Object> redisService;

  @SuppressWarnings("unchecked")
  private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);

  private L2CacheProperties properties;
  private Cache<Object, Object> level1;
  private RedisCaffeineCache cache;

  @BeforeEach
  void setUp() {
    PrincipalContext.set(new Principal().setTenantId(-1L).setOptTenantId(-1L));
    properties = new L2CacheProperties();
    properties.getRedis().setTopic("test-topic");
    level1 = Caffeine.newBuilder().build();
    lenient().when(redisService.getRedisTemplate()).thenReturn(redisTemplate);
    cache = new RedisCaffeineCache(CACHE_NAME, redisService, level1, properties);
  }

  @AfterEach
  void tearDown() {
    PrincipalContext.remove();
  }

  private String redisKey(String logicalKey) {
    return "j2cache:" + CACHE_NAME + ":-1:" + logicalKey;
  }

  @Test
  void protectedConstructorForExtension() {
    RedisCaffeineCache sub = new RedisCaffeineCache(true) {
    };
    assertThat(sub.getName()).isNull();
  }

  @Test
  void getNameAndNativeCache() {
    assertThat(cache.getName()).isEqualTo(CACHE_NAME);
    assertThat(cache.getNativeCache()).isSameAs(cache);
  }

  @Test
  void lookupL2OnlyWhenL1Closed() {
    String k = "a";
    when(redisService.get(redisKey(k))).thenReturn("from-redis");
    assertThat(cache.lookup(k)).isEqualTo("from-redis");
  }

  @Test
  void lookupL1Hit() {
    properties.getComposite().setL1AllOpen(true);
    level1.put("b", "local");
    assertThat(cache.lookup("b")).isEqualTo("local");
    verify(redisService, never()).get(anyString());
  }

  @Test
  void lookupL2HitPromotesToL1WhenEnabled() {
    properties.getComposite().setL1AllOpen(true);
    when(redisService.get(redisKey("c"))).thenReturn("warm");
    assertThat(cache.lookup("c")).isEqualTo("warm");
    assertThat(level1.getIfPresent("c")).isEqualTo("warm");
  }

  @Test
  void lookupL1OpenByManualCacheName() {
    properties.getComposite().setL1Manual(true);
    properties.getComposite().getL1ManualCacheNameSet().add(CACHE_NAME);
    level1.put("d", "m");
    assertThat(cache.lookup("d")).isEqualTo("m");
  }

  @Test
  void lookupL1OpenByManualKey() {
    properties.getComposite().setL1Manual(true);
    properties.getComposite().getL1ManualKeySet().add(redisKey("e"));
    level1.put("e", "mk");
    assertThat(cache.lookup("e")).isEqualTo("mk");
  }

  @Test
  void putStoresRedisWithoutTtlWhenDefaultExpirationZero() {
    String k = "p1";
    cache.put(k, "v1");
    verify(redisService).set(redisKey(k), "v1");
    verify(redisService, never()).set(anyString(), any(), anyLong(), any());
  }

  @Test
  void putUsesPerCacheExpire() {
    properties.getRedis().getExpires().put(CACHE_NAME, 12_345L);
    String k = "p2";
    cache.put(k, "v2");
    verify(redisService).set(eq(redisKey(k)), eq("v2"), eq(12_345L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void putWithL1AllOpenPublishesAndFillsLocal() {
    properties.getComposite().setL1AllOpen(true);
    cache.put("l1k", "lv");
    verify(redisTemplate).convertAndSend(eq("test-topic"), any());
    assertThat(level1.getIfPresent("l1k")).isEqualTo("lv");
  }

  @Test
  void putIgnoresPublishFailure() {
    properties.getComposite().setL1AllOpen(true);
    doThrow(new RuntimeException("pub fail")).when(redisTemplate)
        .convertAndSend(anyString(), any());
    cache.put("pf", "x");
    assertThat(level1.getIfPresent("pf")).isEqualTo("x");
  }

  @Test
  void getCallableLoadsOnce() throws Exception {
    String k = "call";
    // get(key, loader) calls lookup twice before the loader (outer check + double-checked lock).
    // A two-stub sequence (null, loaded) would satisfy the second lookup without loading — use
    // two nulls, then "loaded" for the post-put read on the second get().
    when(redisService.get(redisKey(k))).thenReturn(null, null, "loaded");
    AtomicInteger calls = new AtomicInteger();
    Callable<String> loader = () -> {
      calls.incrementAndGet();
      return "loaded";
    };
    assertThat(cache.get(k, loader)).isEqualTo("loaded");
    assertThat(cache.get(k, loader)).isEqualTo("loaded");
    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  void getCallableWrapsFailure() {
    String k = "bad";
    when(redisService.get(redisKey(k))).thenReturn(null);
    Callable<String> loader = () -> {
      throw new RuntimeException("boom");
    };
    assertThatThrownBy(() -> cache.get(k, loader))
        .satisfies(
            ex -> assertThat(ex.getClass().getSimpleName()).isEqualTo("ValueRetrievalException"));
  }

  @Test
  void putDisallowsNullWhenConfigured() {
    properties.setAllowNullValues(false);
    cache = new RedisCaffeineCache(CACHE_NAME, redisService, level1, properties);
    assertThatThrownBy(() -> cache.put("nk", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not allow null");
  }

  @Test
  void putIfAbsentSetsWhenMissing() {
    String k = "pia";
    when(redisService.get(redisKey(k))).thenReturn(null);
    assertThat(cache.putIfAbsent(k, "first")).isNull();
    verify(redisService).set(redisKey(k), "first");
  }

  @Test
  void putIfAbsentSkipsWhenPresent() {
    String k = "pia2";
    when(redisService.get(redisKey(k))).thenReturn("exists");
    assertThat(cache.putIfAbsent(k, "nope").get()).isEqualTo("exists");
    verify(redisService, never()).set(eq(redisKey(k)), eq("nope"));
  }

  @Test
  void evictSingleKey() {
    cache.evict("ex");
    verify(redisService).delete(redisKey("ex"));
    verify(redisTemplate).convertAndSend(eq("test-topic"), any());
  }

  @Test
  void evictCollection() {
    cache.evict(List.of("x", "y"));
    verify(redisService).delete(redisKey("x"));
    verify(redisService).delete(redisKey("y"));
  }

  @Test
  void clearDeletesKeysAndLocal() {
    when(redisService.keys(CACHE_NAME + ":*")).thenReturn(Set.of(redisKey("1"), redisKey("2")));
    cache.clear();
    verify(redisService).delete(Set.of(redisKey("1"), redisKey("2")));
    assertThat(level1.estimatedSize()).isZero();
  }

  @Test
  void clearWhenNoKeysStillInvalidatesLocalAndPublishes() {
    when(redisService.keys(CACHE_NAME + ":*")).thenReturn(Collections.emptySet());
    level1.put("z", "z");
    cache.clear();
    verify(redisTemplate).convertAndSend(eq("test-topic"), any());
    assertThat(level1.getIfPresent("z")).isNull();
  }

  @Test
  void clearPropagatesRedisFailure() {
    when(redisService.keys(CACHE_NAME + ":*")).thenThrow(new RuntimeException("keys"));
    assertThatThrownBy(() -> cache.clear())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to clear cache");
  }

  @Test
  void clearLocalAllAndSingleKey() {
    level1.put("a", 1);
    level1.put("b", 2);
    cache.clearLocal(null);
    assertThat(level1.estimatedSize()).isZero();
    level1.put("c", 3);
    cache.clearLocal("c");
    assertThat(level1.getIfPresent("c")).isNull();
  }

  @Test
  void emptyPenetrationSafeIgnoresNonEmpty() {
    cache.emptyPenetrationSafe("k", "full");
    verify(redisService, never()).set(anyString(), any(), any(Long.class), any());
  }

  @Test
  void emptyPenetrationSafeEvictsWhenNullsDisallowed() {
    properties.setAllowNullValues(false);
    cache = new RedisCaffeineCache(CACHE_NAME, redisService, level1, properties);
    cache.emptyPenetrationSafe("ep", NullValue.INSTANCE);
    verify(redisService).delete(redisKey("ep"));
  }

  @Test
  void emptyPenetrationSafeStoresWhenMapContainsFullRedisKey() {
    String logical = "pen";
    String full = redisKey(logical);
    properties.getRedis().getPenetrationExpires().put(full, 999L);
    cache = new RedisCaffeineCache(CACHE_NAME, redisService, level1, properties);
    cache.emptyPenetrationSafe(logical, NullValue.INSTANCE);
    verify(redisService).set(eq(full), eq(NullValue.INSTANCE), eq(999L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void getDelegatesToLookup() {
    when(redisService.get(redisKey("gw"))).thenReturn("in-redis");
    assertThat(cache.get("gw").get()).isEqualTo("in-redis");
  }
}
