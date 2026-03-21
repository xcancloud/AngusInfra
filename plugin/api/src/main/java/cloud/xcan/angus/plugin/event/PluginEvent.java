package cloud.xcan.angus.plugin.event;

import cloud.xcan.angus.plugin.model.PluginState;
import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for all plugin lifecycle events.
 * <p>
 * Plugins emit these events at key lifecycle transitions (load, start, stop, unload, fail).
 * Listeners can subscribe to specific event types to react to plugin state changes.
 */
public abstract class PluginEvent extends ApplicationEvent {

  private final String pluginId;
  private final String pluginName;
  private final String pluginVersion;
  private final Instant timestamp;

  public PluginEvent(Object source, String pluginId, String pluginName, String pluginVersion) {
    super(source);
    this.pluginId = pluginId;
    this.pluginName = pluginName;
    this.pluginVersion = pluginVersion;
    this.timestamp = Instant.now();
  }

  public String getPluginId() {
    return pluginId;
  }

  public String getPluginName() {
    return pluginName;
  }

  public String getPluginVersion() {
    return pluginVersion;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public abstract PluginState getEventState();
}
