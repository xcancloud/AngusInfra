package cloud.xcan.angus.plugin.management;

import cloud.xcan.angus.plugin.api.PluginManager;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;
import java.util.List;

public class PluginManagementServiceImpl implements PluginManagementService {

  private final PluginManager pluginManager;

  public PluginManagementServiceImpl(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  @Override
  public void initialize() {
    pluginManager.initialize();
  }

  @Override
  public void reloadAll() {
    pluginManager.loadAllPlugins();
  }

  @Override
  public PluginInfo install(String pluginId, byte[] data) throws PluginException {
    boolean ok = pluginManager.installPlugin(pluginId, data);
    if (!ok) {
      return null;
    }
    return pluginManager.getPluginInfo(pluginId);
  }

  @Override
  public void remove(String pluginId, boolean fromStore) throws PluginException {
    boolean success = pluginManager.removePlugin(pluginId, fromStore);
    if (!success) {
      throw new PluginException("Failed to remove plugin: " + pluginId);
    }
  }

  @Override
  public List<PluginInfo> listPlugins() {
    return pluginManager.getAllPlugins();
  }

  @Override
  public PluginInfo getPlugin(String pluginId) {
    return pluginManager.getPluginInfo(pluginId);
  }

  @Override
  public PluginStats stats() {
    List<PluginInfo> all = pluginManager.getAllPlugins();
    int total = all.size();
    int endpoints = all.stream().mapToInt(PluginInfo::getEndpointCount).sum();
    int active = (int) all.stream().filter(p -> p.getEndpointCount() > 0).count();
    return new PluginStats(total, active, endpoints);
  }
}
