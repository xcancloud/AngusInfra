package cloud.xcan.angus.plugin.api;

import org.springframework.context.ApplicationContext;

import java.nio.file.Path;
import java.util.Map;

public interface PluginContext {
    ApplicationContext getApplicationContext();

    Map<String, Object> getConfiguration();

    Path getDataDirectory();

    void log(String level, String message);

    void log(String level, String message, Throwable throwable);

    void registerService(String name, Object service);

    <T> T getService(String name, Class<T> type);

    <T> T getBean(Class<T> type);

    String getPluginId();

    String getEnvironment(String key);

    String getEnvironment(String key, String defaultValue);
}

