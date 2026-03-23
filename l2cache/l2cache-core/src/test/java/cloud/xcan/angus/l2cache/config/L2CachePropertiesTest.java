package cloud.xcan.angus.l2cache.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class L2CachePropertiesTest {

  @Test
  void defaultsAndNestedMutation() {
    L2CacheProperties p = new L2CacheProperties();
    assertThat(p.getEnabled()).isFalse();
    assertThat(p.isAllowNullValues()).isTrue();
    assertThat(p.isDynamic()).isTrue();
    assertThat(p.getCacheNames()).isEmpty();
    assertThat(p.getComposite().isL1AllOpen()).isFalse();
    assertThat(p.getCaffeine().getExpireAfterAccess()).isZero();
    assertThat(p.getRedis().getDefaultExpiration()).isZero();
    assertThat(p.getRedis().getTopic()).isEqualTo("l2cache:topic");

    p.setEnabled(true).setDynamic(false);
    p.getCacheNames().add("a");
    p.getComposite().setL1AllOpen(true).setL1Manual(true);
    p.getComposite().getL1ManualCacheNameSet().add("users");
    p.getComposite().getL1ManualKeySet().add("k1");
    p.getCaffeine().setExpireAfterWrite(30).setMaximumSize(100);
    p.getRedis().setDefaultExpiration(60_000L);
    p.getRedis().getExpires().put("users", 120_000L);
    p.getRedis().getPenetrationExpires().put("users", 10_000L);

    assertThat(p.getEnabled()).isTrue();
    assertThat(p.isDynamic()).isFalse();
    assertThat(p.getCacheNames()).containsExactly("a");
    assertThat(p.getComposite().isL1Manual()).isTrue();
    assertThat(p.getCaffeine().getExpireAfterWrite()).isEqualTo(30);
    assertThat(p.getRedis().getDefaultExpiration()).isEqualTo(60_000L);
    assertThat(p.getRedis().getExpires()).containsEntry("users", 120_000L);
  }

  @Test
  void configMarkerInterfacesAreImplemented() {
    assertThat(new L2CacheProperties.Composite()).isInstanceOf(L2CacheProperties.Config.class);
    assertThat(new L2CacheProperties.Caffeine()).isInstanceOf(L2CacheProperties.Config.class);
    assertThat(new L2CacheProperties.Redis()).isInstanceOf(L2CacheProperties.Config.class);
  }

  @Test
  void redisExpiresMapsAreMutable() {
    L2CacheProperties p = new L2CacheProperties();
    Map<String, Long> ex = p.getRedis().getExpires();
    Map<String, Long> pen = p.getRedis().getPenetrationExpires();
    ex.put("c", 1L);
    pen.put("c", 2L);
    assertThat(ex).containsEntry("c", 1L);
    assertThat(pen).containsEntry("c", 2L);
  }

  @Test
  void caffeineInitialCapacityAndRefresh() {
    L2CacheProperties p = new L2CacheProperties();
    p.getCaffeine().setInitialCapacity(16).setRefreshAfterWrite(5).setExpireAfterAccess(10);
    assertThat(p.getCaffeine().getInitialCapacity()).isEqualTo(16);
    assertThat(p.getCaffeine().getRefreshAfterWrite()).isEqualTo(5);
    assertThat(p.getCaffeine().getExpireAfterAccess()).isEqualTo(10);
  }

  @Test
  void cacheNamesSetReplacement() {
    L2CacheProperties p = new L2CacheProperties();
    p.setCacheNames(Set.of("x", "y"));
    assertThat(p.getCacheNames()).containsExactlyInAnyOrder("x", "y");
  }
}
