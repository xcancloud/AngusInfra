package cloud.xcan.angus.plugin.model;

/**
 * Plugin lifecycle states used across plugin modules.
 */
public enum PluginState {
    UNKNOWN,
    LOADING,
    INITIALIZED,
    STARTED,
    STOPPED,
    UNLOADING,
    ERROR;

    public static PluginState from(String name) {
        if (name == null) return UNKNOWN;
        try {
            return PluginState.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}

