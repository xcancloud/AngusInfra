package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Plugin base interface
 */
public interface Plugin {

    String getId();

    String getName();

    String getVersion();

    String getDescription();

    String getAuthor();

    void initialize(PluginContext context) throws PluginException;

    void start() throws PluginException;

    void stop() throws PluginException;

    void destroy() throws PluginException;

    PluginState getState();
}

