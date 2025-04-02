package cloud.xcan.angus.web;

import cloud.xcan.angus.core.job.SyncJobTemplate;
import cloud.xcan.angus.lettucex.distlock.RedisLock;
import cloud.xcan.angus.spec.experimental.DistributedLock;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Job Template support.
 *
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(RedisAutoConfigurer.class)
@ConditionalOnProperty(name = "xcan.job.enabled", havingValue = "true", matchIfMissing = true)
public class JobTemplateAutoConfigurer {

  @ConditionalOnMissingBean
  @Bean(name = "distributedLock")
  public DistributedLock distributedLock(StringRedisTemplate stringRedisTemplate) {
    return new RedisLock(stringRedisTemplate);
  }

  @ConditionalOnMissingBean
  @Bean(name = "jobTemplate")
  public SyncJobTemplate jobTemplate(DistributedLock distributedLock){
    return new SyncJobTemplate(distributedLock);
  }

}
