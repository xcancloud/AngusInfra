package cloud.xcan.angus.l2cache;


import cloud.xcan.angus.core.jackson.Jackson2JsonRedisSerializer;
import cloud.xcan.angus.l2cache.config.L2CacheProperties;
import cloud.xcan.angus.l2cache.spring.RedisCaffeineCacheManager;
import cloud.xcan.angus.l2cache.synchronous.CacheMessageListener;
import cloud.xcan.angus.lettucex.util.RedisService;
import cloud.xcan.angus.spec.jackson.EnumModule;
import cloud.xcan.angus.spec.jackson.serializer.BigDecimalDeSerializer;
import cloud.xcan.angus.spec.jackson.serializer.BigDecimalSerializer;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueDeSerializer;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueSerializer;
import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;

@EnableCaching
@Configuration
//@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(L2CacheProperties.class)
@ConditionalOnProperty(prefix = "xcan.l2cache", name = "enabled", matchIfMissing = false)
public class L2CacheAutoConfigurer {

  @Bean("l2cacheRedisObjectMapper")
  @ConditionalOnMissingBean(name = "l2cacheRedisObjectMapper")
  public ObjectMapper l2cacheRedisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Long.TYPE, new ToStringSerializer(Long.TYPE));
    simpleModule.addSerializer(Long.class, new ToStringSerializer(Long.class));
    simpleModule.addSerializer(TimeValue.class, new TimeValueSerializer());
    simpleModule.addDeserializer(TimeValue.class, new TimeValueDeSerializer());
    simpleModule.addSerializer(BigDecimal.class, new BigDecimalSerializer());
    simpleModule.addDeserializer(BigDecimal.class, new BigDecimalDeSerializer());
    mapper.registerModule(simpleModule);
    mapper.registerModule(new EnumModule());
    mapper.registerModule(new CoreJackson2Module());
    mapper.registerModule(new JavaTimeModule());

    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // @JsonCreator -> See io.swagger.v3.oas.models.parameters.Parameter#StyleEnum
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    return mapper;
  }

  @Primary
  @Bean("l2cacheRedisTemplate")
  @ConditionalOnMissingBean(name = "l2cacheRedisTemplate")
  public RedisTemplate<String, Object> l2cacheRedisTemplate(
      @Qualifier("l2cacheRedisObjectMapper") ObjectMapper l2cacheRedisObjectMapper,
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);

    Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(
        Object.class, l2cacheRedisObjectMapper);
    template.setKeySerializer(RedisSerializer.string());
    template.setHashKeySerializer(RedisSerializer.string());
    template.setValueSerializer(jsonSerializer);
    template.setHashValueSerializer(jsonSerializer);
    return template;
  }

  @Bean("l2cacheRedisService")
  @ConditionalOnMissingBean(name = "l2cacheRedisService")
  public RedisService<Object> redisCache(
      @Qualifier("l2cacheRedisTemplate") RedisTemplate<String, Object> l2cacheRedisTemplate) {
    RedisService<Object> redisCache = new RedisService<>();
    redisCache.setRedisTemplate(l2cacheRedisTemplate);
    return redisCache;
  }

  @Bean("l2cacheRedisCaffeineCacheManager")
  @Primary
  @ConditionalOnClass(RedisService.class)
  public CacheManager l2cacheRedisCaffeineCacheManager(
      L2CacheProperties l2CacheProperties,
      @Qualifier("l2cacheRedisService") RedisService<Object> l2cacheRedisService) {
    return new RedisCaffeineCacheManager(l2CacheProperties, l2cacheRedisService);
  }

  @Bean
  @ConditionalOnClass(RedisService.class)
  public RedisMessageListenerContainer redisMessageListenerContainer(
      L2CacheProperties l2CacheProperties,
      @Qualifier("l2cacheRedisCaffeineCacheManager") CacheManager l2cacheRedisCaffeineCacheManager,
      @Qualifier("l2cacheRedisService") RedisService<Object> l2cacheRedisService,
      @Qualifier("l2cacheRedisObjectMapper") ObjectMapper l2cacheRedisObjectMapper) {
    RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
    redisMessageListenerContainer
        .setConnectionFactory(
            Objects.requireNonNull(l2cacheRedisService.getRedisTemplate().getConnectionFactory()));
    CacheMessageListener cacheMessageListener = new CacheMessageListener(
        (RedisCaffeineCacheManager) l2cacheRedisCaffeineCacheManager, l2cacheRedisObjectMapper);
    redisMessageListenerContainer.addMessageListener(cacheMessageListener,
        new ChannelTopic(l2CacheProperties.getRedis().getTopic()));
    return redisMessageListenerContainer;
  }

}
