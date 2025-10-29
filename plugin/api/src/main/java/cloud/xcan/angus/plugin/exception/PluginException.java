package cloud.xcan.angus.plugin.exception;

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

