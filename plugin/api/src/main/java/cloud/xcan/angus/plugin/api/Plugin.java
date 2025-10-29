package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Plugin base interface.
 * <p>
 * Implementations of this interface represent a plugin that can be managed by the
 * plugin framework. A plugin has basic lifecycle methods (initialize/start/stop/destroy)
 * and metadata (id, name, version, author, description). The plugin may throw
 * PluginException for lifecycle failures.
 */
public interface Plugin {

    /**
     * Return a unique identifier for this plugin.
     *
     * @return plugin id (should be globally unique within the host)
     */
    String getId();

    /**
     * Return a human-readable name for the plugin.
     *
     * @return plugin name
     */
    String getName();

    /**
     * Return the plugin version string (for example "1.0.0").
     *
     * @return version string
     */
    String getVersion();

    /**
     * Short description of the plugin's purpose or functionality.
     *
     * @return description text
     */
    String getDescription();

    /**
     * Author or vendor of the plugin.
     *
     * @return author name or organization
     */
    String getAuthor();

    /**
     * Initialize the plugin with the given context. This method is called once when
     * the plugin is loaded. Implementations should perform one-time setup here,
     * such as reading configuration from the provided PluginContext, registering
     * services, or preparing resources.
     *
     * @param context plugin runtime context provided by the host
     * @throws PluginException when initialization fails
     */
    void initialize(PluginContext context) throws PluginException;

    /**
     * Start the plugin. Called after initialization to make the plugin active.
     * Implementations should start internal workers, schedule tasks, or register
     * runtime endpoints here.
     *
     * @throws PluginException when start fails
     */
    void start() throws PluginException;

    /**
     * Stop the plugin. Called when the plugin should cease activity but before it is
     * destroyed. Implementations should stop background tasks and release runtime
     * resources acquired in start().
     *
     * @throws PluginException when stop fails
     */
    void stop() throws PluginException;

    /**
     * Destroy the plugin and release all resources. After destroy is called the plugin
     * instance may be unloaded and should not be used again. Implementations should
     * free any remaining resources and perform final cleanup.
     *
     * @throws PluginException when cleanup fails
     */
    void destroy() throws PluginException;

    /**
     * Return the current lifecycle state of the plugin (for example INITIALIZED, STARTED, STOPPED).
     * The PluginState enum represents these states and can be used by the host to
     * track plugin health and lifecycle transitions.
     *
     * @return current plugin state
     */
    PluginState getState();
}
