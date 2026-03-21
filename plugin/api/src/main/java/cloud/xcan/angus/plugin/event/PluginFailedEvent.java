package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;
import lombok.Getter;

/**
 * Event fired when a plugin lifecycle operation fails (load, start, stop, destroy).
 */
@Getter
public class PluginFailedEvent extends PluginEvent {

  private final String failureReason;
  private final Throwable exception;

  public PluginFailedEvent(Object source, String pluginId, String pluginName,
      String pluginVersion, String failureReason, Throwable exception) {
    super(source, pluginId, pluginName, pluginVersion);
    this.failureReason = failureReason;
    this.exception = exception;
  }

  @Override
  public PluginState getEventState() {
    return PluginState.ERROR;
  }
}
