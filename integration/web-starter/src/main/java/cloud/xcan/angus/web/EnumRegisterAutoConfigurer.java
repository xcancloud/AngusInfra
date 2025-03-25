package cloud.xcan.angus.web;

import cloud.xcan.angus.core.enums.EnumStore;
import cloud.xcan.angus.core.enums.EnumStoreInMemory;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.web.endpoint.EnumEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ApplicationInfo.class})
@ConditionalOnProperty(name = "xcan.enums.enabled", havingValue = "true", matchIfMissing = true)
public class EnumRegisterAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public EnumEndpoint enumEndpoint(EnumStore enumStore) {
    return new EnumEndpoint(enumStore);
  }

  @Bean
  @ConditionalOnMissingBean
  public EnumStore enumStore(ApplicationInfo applicationInfo) {
    return new EnumStoreInMemory(applicationInfo);
  }

}
