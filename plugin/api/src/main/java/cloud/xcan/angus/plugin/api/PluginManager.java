package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;

import java.nio.file.Path;
import java.util.List;

public interface PluginManager {
    void initialize();

    void loadAllPlugins();

    boolean loadPlugin(Path pluginPath) throws PluginException;

    boolean startPlugin(String pluginId) throws PluginException;

    boolean stopPlugin(String pluginId) throws PluginException;

    boolean unloadPlugin(String pluginId) throws PluginException;

    boolean reloadPlugin(String pluginId) throws PluginException;

    List<PluginInfo> getAllPlugins();

    PluginInfo getPluginInfo(String pluginId);

    boolean hasPlugin(String pluginId);

    // Management operations: install plugin bytes into store and load
    boolean installPlugin(String pluginId, byte[] data) throws PluginException;

    // Remove plugin from manager and optionally from store
    boolean removePlugin(String pluginId, boolean removeFromStore) throws PluginException;
}
