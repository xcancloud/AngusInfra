package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Event fired when a plugin has been unloaded and removed from the manager.
 */
public class PluginUnloadedEvent extends PluginEvent {

  public PluginUnloadedEvent(Object source, String pluginId, String pluginName,
      String pluginVersion) {
    super(source, pluginId, pluginName, pluginVersion);
  }

  @Override
  public PluginState getEventState() {
    return PluginState.UNLOADING;
  }
}
