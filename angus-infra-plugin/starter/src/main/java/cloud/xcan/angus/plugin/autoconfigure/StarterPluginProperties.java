package cloud.xcan.angus.plugin.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Starter module binds PluginProperties from configuration into this class and exposes it as a
 * bean.
 */
@ConfigurationProperties(prefix = "angus.plugin")
public class StarterPluginProperties extends PluginProperties {

}

