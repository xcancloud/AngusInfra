package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Event fired when a plugin has been successfully started and is now active.
 */
public class PluginStartedEvent extends PluginEvent {

  public PluginStartedEvent(Object source, String pluginId, String pluginName,
      String pluginVersion) {
    super(source, pluginId, pluginName, pluginVersion);
  }

  @Override
  public PluginState getEventState() {
    return PluginState.STARTED;
  }
}
