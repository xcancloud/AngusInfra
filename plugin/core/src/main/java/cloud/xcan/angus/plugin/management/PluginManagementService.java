package cloud.xcan.angus.plugin.management;

import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;
import java.util.List;

public interface PluginManagementService {

  /**
   * Initialize the plugin management system, loading all plugins from store.
   */
  void initialize();

  /**
   * Reload all plugins from persistent store.
   */
  void reloadAll();

  /**
   * Install plugin bytes into store and load it; returns PluginInfo for the installed plugin.
   */
  PluginInfo install(String pluginId, byte[] data) throws PluginException;

  /**
   * Remove plugin and optionally delete from persistent store.
   */
  void remove(String pluginId, boolean fromStore) throws PluginException;

  /**
   * List plugins currently known to the manager.
   */
  List<PluginInfo> listPlugins();

  /**
   * Get plugin details by id, or null if not found.
   */
  PluginInfo getPlugin(String pluginId);

  /**
   * Get plugin statistics.
   */
  PluginStats stats();
}
