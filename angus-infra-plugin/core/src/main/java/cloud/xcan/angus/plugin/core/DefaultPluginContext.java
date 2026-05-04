package cloud.xcan.angus.plugin.core;

import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.model.PluginDescriptor;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class DefaultPluginContext implements PluginContext {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginContext.class);
  private final Logger pluginLog;
  private final ApplicationContext applicationContext;
  private final PluginDescriptor descriptor;
  private final Map<String, Object> configuration;
  private final PluginProperties properties;
  private final Map<String, Object> services = new ConcurrentHashMap<>();

  public DefaultPluginContext(ApplicationContext applicationContext,
      PluginDescriptor descriptor,
      Map<String, Object> configuration,
      PluginProperties properties) {
    this.applicationContext = applicationContext;
    this.descriptor = descriptor;
    this.configuration = configuration;
    this.properties = properties;
    this.pluginLog = LoggerFactory.getLogger("plugin." + descriptor.getId());
  }

  @Override
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  @Override
  public Path getDataDirectory() {
    return Path.of(properties.getDataDirectory()).resolve(descriptor.getId());
  }

  @Override
  public void log(String level, String message) {
    if (level == null || message == null) {
      return;
    }
    switch (level.toUpperCase()) {
      case "DEBUG" -> pluginLog.debug(message);
      case "INFO" -> pluginLog.info(message);
      case "WARN" -> pluginLog.warn(message);
      case "ERROR" -> pluginLog.error(message);
      default -> pluginLog.info(message);
    }
  }

  @Override
  public void log(String level, String message, Throwable throwable) {
    if (level == null || message == null) {
      return;
    }
    switch (level.toUpperCase()) {
      case "DEBUG" -> pluginLog.debug(message, throwable);
      case "INFO" -> pluginLog.info(message, throwable);
      case "WARN" -> pluginLog.warn(message, throwable);
      case "ERROR" -> pluginLog.error(message, throwable);
      default -> pluginLog.info(message, throwable);
    }
  }

  @Override
  public void registerService(String name, Object service) {
    services.put(name, service);
  }

  @Override
  public <T> T getService(String name, Class<T> type) {
    return type.cast(services.get(name));
  }

  @Override
  public <T> T getBean(Class<T> type) {
    return applicationContext.getBean(type);
  }

  @Override
  public String getPluginId() {
    return descriptor.getId();
  }

  @Override
  public String getEnvironment(String key) {
    return System.getenv(key);
  }

  @Override
  public String getEnvironment(String key, String defaultValue) {
    String v = System.getenv(key);
    return v == null ? defaultValue : v;
  }
}

