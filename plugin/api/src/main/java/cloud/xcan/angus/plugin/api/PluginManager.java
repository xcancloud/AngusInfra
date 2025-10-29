package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * Main plugin manager API used by the host to control plugins.
 * <p>
 * Implementations are responsible for discovering, loading, starting, stopping
 * and unloading plugin artifacts. The manager also exposes management operations
 * such as installing plugin bytes into storage and removing plugins.
 */
public interface PluginManager {

    /**
     * Initialize the plugin manager. This should create necessary directories and
     * prepare internal data structures; it does not necessarily load plugins.
     */
    void initialize();

    /**
     * Scan configured sources (disk or store) and load all available plugins.
     */
    void loadAllPlugins();

    /**
     * Load a single plugin jar from the given file system path.
     *
     * @param pluginPath path to plugin jar
     * @return true when load succeeded
     * @throws PluginException on failure
     */
    boolean loadPlugin(Path pluginPath) throws PluginException;

    /**
     * Start a loaded plugin so it becomes active.
     *
     * @param pluginId plugin identifier
     * @return true when started successfully
     * @throws PluginException on failure
     */
    boolean startPlugin(String pluginId) throws PluginException;

    /**
     * Stop a running plugin.
     *
     * @param pluginId plugin identifier
     * @return true when stopped successfully
     * @throws PluginException on failure
     */
    boolean stopPlugin(String pluginId) throws PluginException;

    /**
     * Unload a previously loaded plugin and free its resources.
     *
     * @param pluginId plugin identifier
     * @return true when unloaded
     * @throws PluginException on failure
     */
    boolean unloadPlugin(String pluginId) throws PluginException;

    /**
     * Reload a plugin by id (unload then load again).
     *
     * @param pluginId plugin identifier
     * @return true when reload completed
     * @throws PluginException on failure
     */
    boolean reloadPlugin(String pluginId) throws PluginException;

    /**
     * Return a list of plugin infos for all currently loaded plugins.
     *
     * @return plugin info list
     */
    List<PluginInfo> getAllPlugins();

    /**
     * Get detailed PluginInfo for the given plugin id, or null if not found.
     *
     * @param pluginId plugin identifier
     * @return PluginInfo or null
     */
    PluginInfo getPluginInfo(String pluginId);

    /**
     * Query whether a plugin with the given id is loaded.
     *
     * @param pluginId plugin identifier
     * @return true when plugin is present
     */
    boolean hasPlugin(String pluginId);

    /**
     * Install plugin bytes into configured store (disk or database) and attempt to load it.
     * Implementations should persist the bytes according to the configured PluginStore
     * and then call the loading routine.
     *
     * @param pluginId plugin identifier
     * @param data     raw plugin jar bytes
     * @return true when installation and load succeeded
     * @throws PluginException on failure
     */
    boolean installPlugin(String pluginId, byte[] data) throws PluginException;

    /**
     * Remove a plugin from the manager and optionally delete its persisted artifact from store.
     *
     * @param pluginId        plugin identifier
     * @param removeFromStore when true, delete persisted plugin artifact from storage
     * @return true when removal succeeded
     * @throws PluginException on failure
     */
    boolean removePlugin(String pluginId, boolean removeFromStore) throws PluginException;
}
