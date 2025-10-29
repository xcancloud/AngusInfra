package cloud.xcan.angus.plugin.exception;

/**
 * General exception type for plugin lifecycle and management errors.
 * <p>
 * Implementations and the plugin manager may throw this when operations such as
 * initialize/start/stop/install/remove fail for a plugin.
 */
public class PluginException extends Exception {
    public PluginException() {
        super();
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
