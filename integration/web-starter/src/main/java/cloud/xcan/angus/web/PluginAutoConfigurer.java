package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.plugin.core.PluginStateListener;
import cloud.xcan.angus.plugin.core.PluginWrapper;
import cloud.xcan.angus.plugin.spring.SpringPluginManager;
import cloud.xcan.angus.spec.version.DefaultVersionManager;
import cloud.xcan.angus.spec.version.VersionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SpringPluginManager.class)
@ConditionalOnProperty(prefix = "xcan.plugin", name = "enabled", matchIfMissing = true)
public class PluginAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public SpringPluginManager pluginManager(
      @Autowired(required = false) List<PluginStateListener> listeners) {
    SpringPluginManager springPluginManager = new SpringPluginManager();
    if (isNotEmpty(listeners)) {
      for (PluginStateListener pluginStateListener : listeners) {
        springPluginManager.addPluginStateListener(pluginStateListener);
      }
    }
    return springPluginManager;
  }

  @Bean
  @ConditionalOnMissingBean
  public VersionManager versionManager() {
    return new DefaultVersionManager();
  }

  @Bean
  public PluginEndpoint pluginEndpoint(SpringPluginManager pluginManager) {
    return new PluginEndpoint(pluginManager);
  }
}

@Endpoint(id = "plugins")
@ConditionalOnClass(SpringPluginManager.class)
class PluginEndpoint {

  private final SpringPluginManager pluginManager;

  public PluginEndpoint(SpringPluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  /**
   * Query the information of loaded plugins in the current application instance.
   */
  @ReadOperation
  public Map<String, Map<String, Object>> pluginAll() {
    Map<String, Map<String, Object>> pluginMap = new HashMap<>();
    for (PluginWrapper plugin : pluginManager.getPlugins()) {
      pluginMap.put(plugin.getPluginId(),
          Map.of(new Str0(new long[]{0x759AC35D8A189C04L, 0x34F4D1AC37DFD1F8L, 0x3B6BDFFB0CEBD816L})
                  .toString() /* => "descriptor" */, plugin.getDescriptor(),
              new Str0(new long[]{0xCD88767FBB7E8DB0L, 0x2134E1FC3DBFAD22L})
                  .toString() /* => "path" */, plugin.getPluginPath(),
              new Str0(new long[]{0xDDB175E44576F413L, 0x4D63FD3A4605862BL})
                  .toString() /* => "state" */, plugin.getPluginState(),
              new Str0(new long[]{0x5C21414BA0EBBB4BL, 0xFB9074B0C50E4064L, 0x7243C44FA7C35E34L})
                  .toString() /* => "runtimeMode" */, plugin.getRuntimeMode()));
    }
    return pluginMap;
  }

}
