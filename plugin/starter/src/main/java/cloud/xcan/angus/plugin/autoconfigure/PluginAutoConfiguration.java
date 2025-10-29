package cloud.xcan.angus.plugin.autoconfigure;

import cloud.xcan.angus.plugin.api.PluginManager;
import cloud.xcan.angus.plugin.core.DefaultPluginManager;
import cloud.xcan.angus.plugin.core.DynamicRestEndpointManager;
import cloud.xcan.angus.plugin.jpa.JpaPluginStore;
import cloud.xcan.angus.plugin.jpa.PluginRepository;
import cloud.xcan.angus.plugin.management.PluginManagementService;
import cloud.xcan.angus.plugin.management.PluginManagementServiceImpl;
import cloud.xcan.angus.plugin.store.DiskPluginStore;
import cloud.xcan.angus.plugin.store.PluginStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableConfigurationProperties(StarterPluginProperties.class)
@ConditionalOnProperty(prefix = "angus.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PluginAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(RequestMappingHandlerMapping.class)
  public DynamicRestEndpointManager dynamicRestEndpointManager(
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      ApplicationContext applicationContext) {
    return new DynamicRestEndpointManager(requestMappingHandlerMapping, applicationContext);
  }

  @Bean
  @ConditionalOnMissingBean
  public PluginStore pluginStore(ApplicationContext applicationContext,
      StarterPluginProperties properties) {
    if (PluginProperties.StorageType.JPA.equals(properties.getStorageType())) {
      try {
        PluginRepository repo = applicationContext.getBean(PluginRepository.class);
        return new JpaPluginStore(repo);
      } catch (Exception ignored) {
      }
    }
    return new DiskPluginStore(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public DefaultPluginManager defaultPluginManager(ApplicationContext applicationContext,
      PluginStore pluginStore, StarterPluginProperties properties) {
    DynamicRestEndpointManager restEndpointManager = null;
    try {
      restEndpointManager = applicationContext.getBean(DynamicRestEndpointManager.class);
    } catch (Exception ignored) {
    }

    DefaultPluginManager mgr = new DefaultPluginManager(applicationContext, properties,
        restEndpointManager);
    mgr.setPluginStore(pluginStore);
    return mgr;
  }

  @Bean
  @ConditionalOnMissingBean
  public PluginManagementService pluginManagementService(ApplicationContext applicationContext) {
    try {
      PluginManager pm = applicationContext.getBean(PluginManager.class);
      return new PluginManagementServiceImpl(pm);
    } catch (Exception e) {
      return null;
    }
  }
}
