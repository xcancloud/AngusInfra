package cloud.xcan.angus.l2cache;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.lettucex.util.RedisService;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

class L2CacheAutoConfigurerTest {

  @Test
  void l2cacheRedisObjectMapperRegistersModules() {
    ObjectMapper om = new L2CacheAutoConfigurer().l2cacheRedisObjectMapper();
    assertThat(om.getRegisteredModuleIds()).isNotEmpty();
    assertThat(om.getDeserializationConfig().isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS))
        .isTrue();
  }

  @Test
  void redisCacheServiceUsesProvidedTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    RedisService<Object> svc = new L2CacheAutoConfigurer().redisCache(template);
    assertThat(svc.getRedisTemplate()).isSameAs(template);
  }

  @Test
  void l2cacheRedisTemplateUsesJsonSerializers() {
    L2CacheAutoConfigurer cfg = new L2CacheAutoConfigurer();
    ObjectMapper om = cfg.l2cacheRedisObjectMapper();
    RedisConnectionFactory cf = org.mockito.Mockito.mock(RedisConnectionFactory.class);
    RedisTemplate<String, Object> tpl = cfg.l2cacheRedisTemplate(om, cf);
    assertThat(tpl.getConnectionFactory()).isSameAs(cf);
    assertThat(tpl.getValueSerializer()).isNotNull();
    assertThat(tpl.getKeySerializer()).isNotNull();
  }
}
