package cloud.xcan.angus.idgen;

import cloud.xcan.angus.api.pojo.instance.InstanceInfo;
import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.core.jpa.identity.SnowflakeIdGenerator;
import cloud.xcan.angus.idgen.bid.ConfigIdAssigner;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.impl.DefaultBidGenerator;
import cloud.xcan.angus.idgen.dao.IdConfigRepo;
import cloud.xcan.angus.idgen.dao.InstanceRepo;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;

@Configuration
@Conditional({IdGenAutoConfigurer.CoreCondition.class})
public class IdGenAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public InstanceInfoConfig instanceInfoConfig() {
    return new InstanceInfoConfig();
  }

  @Bean
  @ConditionalOnMissingBean
  public DisposableInstanceIdAssigner instanceIdAssigner(InstanceRepo instanceRepo) {
    return new DisposableInstanceIdAssigner(instanceRepo);
  }

  @Bean
  @DependsOn("dataSourceInitializer")
  @ConditionalOnMissingBean
  public CachedUidGenerator cachedUidGenerator(InstanceRepo instanceRepo,
      InstanceInfoConfig configurer/*, DisposableInstanceIdAssigner instanceIdAssigner*/)
      throws Exception {
    CachedUidGenerator generator = new CachedUidGenerator();
    generator.setInstanceIdAssigner(instanceIdAssigner(instanceRepo));
    generator.setInstanceInfo(new InstanceInfo() {
      @Override
      public InstanceType getInstanceType() {
        return configurer.getEnv();
      }

      @Override
      public String getHost() {
        return configurer.getHost();
      }

      @Override
      public String getPort() {
        return configurer.getPort();
      }
    });
    SnowflakeIdGenerator.setUidGenerator(generator);
    generator.afterPropertiesSet();
    return generator;
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigIdAssigner disposableConfigIdAssigner(IdConfigRepo idConfigRepo) {
    return new DisposableConfigIdAssigner(idConfigRepo);
  }

  @Bean
  @ConditionalOnMissingBean
  public DistributedIncrAssigner distributedIncrAssigner(
      @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
    return (generatorKey, i) -> redisTemplate.opsForValue().increment(generatorKey, i);
  }

  @Bean
  @DependsOn({"dataSourceInitializer"})
  @ConditionalOnMissingBean
  public BidGenerator bidGenerator(ConfigIdAssigner configIdAssigner,
      @Autowired(required = false) DistributedIncrAssigner distributedIncrAssigner) {
    return new DefaultBidGenerator(configIdAssigner, distributedIncrAssigner);
  }

  static final class CoreCondition implements Condition {

    CoreCondition() {
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata a) {
      String enabled = context.getEnvironment().getProperty("xcan.idgen.enabled");
      return !ObjectUtils.isEmpty(enabled) && Boolean.parseBoolean(enabled);
    }
  }
}
