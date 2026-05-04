package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for all plugin lifecycle events.
 * <p>
 * Plugins emit these events at key lifecycle transitions (load, start, stop, unload, fail).
 * Listeners can subscribe to specific event types to react to plugin state changes.
 */
@Getter
public abstract class PluginEvent extends ApplicationEvent {

  private final String pluginId;
  private final String pluginName;
  private final String pluginVersion;

  public PluginEvent(Object source, String pluginId, String pluginName, String pluginVersion) {
    super(source);
    this.pluginId = pluginId;
    this.pluginName = pluginName;
    this.pluginVersion = pluginVersion;
  }

  public abstract PluginState getEventState();
}
