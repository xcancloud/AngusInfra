package cloud.xcan.angus.web;

import cloud.xcan.sdf.l2cache.L2CacheAutoConfigurer;
import cloud.xcan.sdf.lettucex.config.LettuceConnectionConfiguration;
import cloud.xcan.sdf.lettucex.config.RedisProperties;
import cloud.xcan.sdf.lettucex.util.RedisService;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data's Redis support.
 *
 * @author liuxiaolong
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureBefore(L2CacheAutoConfigurer.class)
@EnableConfigurationProperties(RedisProperties.class)
@Import({LettuceConnectionConfiguration.class})
@ConditionalOnProperty(name = "xcan.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean(name = "redisTemplate")
  @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    RedisSerializer<String> redisSerializer = new StringRedisSerializer();
    // RedisSerializer redisJsonSerializer = new GenericJackson2JsonRedisSerializerricJackson2JsonRedisSerializer();
    template.setKeySerializer(redisSerializer);
    // template.setValueSerializer(redisJsonSerializer);
    template.setHashKeySerializer(redisSerializer);
    // template.setHashValueSerializer(redisJsonSerializer);
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }

  @Bean
  @ConditionalOnMissingBean(name = "stringRedisTemplate")
  @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    RedisSerializer<String> stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    //  redisTemplate.afterPropertiesSet();
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }

  @Bean("redisService")
  @ConditionalOnMissingBean(name = "redisService")
  public RedisService<Object> redisService(RedisTemplate<String, Object> redisTemplate) {
    return new RedisService<>(redisTemplate);
  }

  @Bean("stringRedisService")
  @ConditionalOnMissingBean(name = "stringRedisService")
  public RedisService<String> stringRedisService(StringRedisTemplate stringRedisTemplate) {
    return new RedisService<>(stringRedisTemplate);
  }
}
