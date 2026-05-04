package cloud.xcan.angus.plugin.api;

import java.nio.file.Path;
import java.util.Map;
import org.springframework.context.ApplicationContext;

/**
 * Runtime context provided to a plugin by the host.
 * <p>
 * Implementations expose access to the Spring {@link ApplicationContext}, configuration and data
 * directory for the plugin, simple logging utilities, and a service registry where plugins can
 * publish or look up shared services.
 */
public interface PluginContext {

  /**
   * Return the Spring ApplicationContext used by the host application. Plugins may use this to look
   * up beans or publish their own beans (if appropriate).
   *
   * @return the host ApplicationContext
   */
  ApplicationContext getApplicationContext();

  /**
   * Return the plugin-specific configuration map. This typically contains values from the plugin
   * descriptor merged with host-provided overrides.
   *
   * @return configuration key/value map (never null but may be empty)
   */
  Map<String, Object> getConfiguration();

  /**
   * Directory on disk where the plugin may store persistent or ephemeral data. The host is
   * responsible for creating this directory before the plugin writes to it.
   *
   * @return Path to the plugin data directory
   */
  Path getDataDirectory();

  /**
   * Log a message at the given level using the host's logging infrastructure. Level is typically
   * one of INFO/WARN/ERROR/DEBUG but is not restricted by the API.
   *
   * @param level   textual log level
   * @param message message to log
   */
  void log(String level, String message);

  /**
   * Log a message and associated throwable.
   *
   * @param level     textual log level
   * @param message   message to log
   * @param throwable optional throwable to include
   */
  void log(String level, String message, Throwable throwable);

  /**
   * Register a service object under the given name so other plugins or the host can look it up.
   * Implementations should document any thread-safety or lifecycle semantics.
   *
   * @param name    service name
   * @param service service instance
   */
  void registerService(String name, Object service);

  /**
   * Lookup a named service and cast it to the requested type.
   *
   * @param name service name
   * @param type expected service class
   * @param <T>  expected type
   * @return service instance or null if not found
   */
  <T> T getService(String name, Class<T> type);

  /**
   * Convenience to fetch a Spring bean by type from the host ApplicationContext.
   *
   * @param type bean class
   * @param <T>  type
   * @return bean instance or null if not present
   */
  <T> T getBean(Class<T> type);

  /**
   * Return the plugin id associated with this context.
   *
   * @return plugin id
   */
  String getPluginId();

  /**
   * Lookup an environment/configuration property provided by the host.
   *
   * @param key environment key
   * @return value or null if not found
   */
  String getEnvironment(String key);

  /**
   * Lookup an environment/configuration property with a default value fallback.
   *
   * @param key          environment key
   * @param defaultValue value to return when key not present
   * @return value or defaultValue
   */
  String getEnvironment(String key, String defaultValue);
}
