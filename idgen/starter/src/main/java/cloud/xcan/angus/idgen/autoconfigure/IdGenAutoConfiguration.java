package cloud.xcan.angus.idgen;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.pojo.instance.InstanceInfo;
import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.bid.ConfigIdAssigner;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.impl.DefaultBidGenerator;
import cloud.xcan.angus.idgen.dao.IdConfigRepo;
import cloud.xcan.angus.idgen.dao.InstanceRepo;
import cloud.xcan.angus.idgen.dao.SpringDataIdConfigRepository;
import cloud.xcan.angus.idgen.dao.SpringIdConfigPersistenceAdapter;
import cloud.xcan.angus.idgen.entity.Instance;
import cloud.xcan.angus.idgen.uid.buffer.RejectedPutBufferPolicies;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.persistence.jpa.identity.SnowflakeIdGenerator;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Conditional({IdGenAutoConfigurer.CoreCondition.class})
public class IdGenAutoConfigurer {

  private final IdGenProperties idGenProperties;

  public IdGenAutoConfigurer(IdGenProperties idGenProperties) {
    this.idGenProperties = idGenProperties;
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnBean(EntityManagerFactory.class)
  @EntityScan(basePackageClasses = Instance.class)
  @EnableJpaRepositories(basePackageClasses = InstanceRepo.class)
  static class CacheJpaRepositoryConfiguration {

  }

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
  public CachedUidGenerator cachedUidGenerator(InstanceInfoConfig configurer,
      DisposableInstanceIdAssigner instanceIdAssigner) throws Exception {
    CachedUidGenerator generator = new CachedUidGenerator();

    // Apply UID configuration
    IdGenProperties.UidGeneatorConfig uidConfig = idGenProperties.getUid();
    generator.setTimeBits(uidConfig.getTimeBits());
    generator.setWorkerBits(uidConfig.getWorkerBits());
    generator.setSeqBits(uidConfig.getSeqBits());
    generator.setEpochStr(uidConfig.getEpochStr());
    generator.setRetriesNum(uidConfig.getRetriesNum());

    // Apply cached UID configuration
    IdGenProperties.CachedUidConfig cachedConfig = idGenProperties.getCached();
    generator.setBoostPower(cachedConfig.getBoostPower());
    generator.setScheduleInterval(cachedConfig.getScheduleInterval());

    // Apply rejection policy
    String rejectionPolicy = cachedConfig.getRejectionPolicy();
    if ("EXCEPTION".equalsIgnoreCase(rejectionPolicy)) {
      generator.setRejectedPutBufferHandler(new RejectedPutBufferPolicies.ExceptionPolicy());
    } else if ("DISCARD".equalsIgnoreCase(rejectionPolicy)) {
      generator.setRejectedPutBufferHandler(new RejectedPutBufferPolicies.DiscardPolicy());
    } else {
      // Default to BLOCK policy
      generator.setRejectedPutBufferHandler(new RejectedPutBufferPolicies.BlockPolicy());
    }

    generator.setInstanceIdAssigner(instanceIdAssigner);
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
  @ConditionalOnBean(SpringDataIdConfigRepository.class)
  public IdConfigRepo idConfigRepo(SpringDataIdConfigRepository springDataIdConfigRepository) {
    return new SpringIdConfigPersistenceAdapter(springDataIdConfigRepository);
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
    IdGenProperties.BidGeneratorConfig bidConfig = idGenProperties.getBid();
    return new DefaultBidGenerator(configIdAssigner, distributedIncrAssigner,
        bidConfig.getInitialMapCapacity());
  }

  static final class CoreCondition implements Condition {

    CoreCondition() {
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata a) {
      String enabled = context.getEnvironment().getProperty("angus.idgen.enabled");
      return isNotEmpty(enabled) && Boolean.parseBoolean(enabled);
    }
  }
}
