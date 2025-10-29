package cloud.xcan.angus.plugin.autoconfigure;

import cloud.xcan.angus.plugin.core.DefaultPluginManager;
import cloud.xcan.angus.plugin.core.DynamicRestEndpointManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableConfigurationProperties(PluginProperties.class)
@ConditionalOnProperty(prefix = "angus.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PluginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RequestMappingHandlerMapping.class)
    public DynamicRestEndpointManager dynamicRestEndpointManager(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                                 ApplicationContext applicationContext) {
        return new DynamicRestEndpointManager(requestMappingHandlerMapping, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultPluginManager defaultPluginManager(ApplicationContext applicationContext) {
        PluginProperties properties = null;
        try {
            properties = applicationContext.getBean(PluginProperties.class);
        } catch (Exception ignored) {
        }
        if (properties == null) properties = new PluginProperties();

        DynamicRestEndpointManager restEndpointManager = null;
        try {
            restEndpointManager = applicationContext.getBean(DynamicRestEndpointManager.class);
        } catch (Exception ignored) {
        }

        return new DefaultPluginManager(applicationContext, properties, restEndpointManager);
    }
}
