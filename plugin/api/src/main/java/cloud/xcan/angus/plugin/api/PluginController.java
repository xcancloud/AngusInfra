package cloud.xcan.angus.plugin.api;

import lombok.Setter;
import org.springframework.context.ApplicationContext;

public abstract class PluginController {
    @Setter
    protected PluginContext pluginContext;

    protected ApplicationContext applicationContext;

    protected String getPluginId() {
        return pluginContext != null ? pluginContext.getPluginId() : "unknown";
    }

    protected void log(String level, String message) {
        if (pluginContext != null) pluginContext.log(level, message);
    }

    protected Object getConfig(String key) {
        return pluginContext != null ? pluginContext.getConfiguration().get(key) : null;
    }

    protected <T> T getService(String name, Class<T> type) {
        return pluginContext != null ? pluginContext.getService(name, type) : null;
    }

    protected ApplicationContext getApplicationContext() {
        if (applicationContext == null && pluginContext != null) {
            applicationContext = pluginContext.getApplicationContext();
        }
        return applicationContext;
    }
}

