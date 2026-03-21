package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;

/**
 * Event fired when a plugin has been stopped.
 */
public class PluginStoppedEvent extends PluginEvent {

  public PluginStoppedEvent(Object source, String pluginId, String pluginName,
      String pluginVersion) {
    super(source, pluginId, pluginName, pluginVersion);
  }

  @Override
  public PluginState getEventState() {
    return PluginState.STOPPED;
  }
}
