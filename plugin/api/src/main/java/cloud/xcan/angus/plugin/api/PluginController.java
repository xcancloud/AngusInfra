package cloud.xcan.angus.plugin.api;

import lombok.Setter;
import org.springframework.context.ApplicationContext;

/**
 * Base class for plugin-provided controller components. Plugins may extend this class
 * to obtain access to the plugin's {@link PluginContext} and convenience helpers
 * such as logging and service lookup.
 */
public abstract class PluginController {
    @Setter
    protected PluginContext pluginContext;

    protected ApplicationContext applicationContext;

    /**
     * Return the plugin id associated with this controller (or "unknown" if not initialized).
     */
    protected String getPluginId() {
        return pluginContext != null ? pluginContext.getPluginId() : "unknown";
    }

    /**
     * Convenience wrapper to log through the plugin context.
     */
    protected void log(String level, String message) {
        if (pluginContext != null) pluginContext.log(level, message);
    }

    /**
     * Return configuration value for the provided key from plugin config.
     */
    protected Object getConfig(String key) {
        return pluginContext != null ? pluginContext.getConfiguration().get(key) : null;
    }

    /**
     * Shortcut to resolve a named service registered via PluginContext.
     */
    protected <T> T getService(String name, Class<T> type) {
        return pluginContext != null ? pluginContext.getService(name, type) : null;
    }

    /**
     * Lazy access to Spring ApplicationContext from the plugin context.
     */
    protected ApplicationContext getApplicationContext() {
        if (applicationContext == null && pluginContext != null) {
            applicationContext = pluginContext.getApplicationContext();
        }
        return applicationContext;
    }
}
