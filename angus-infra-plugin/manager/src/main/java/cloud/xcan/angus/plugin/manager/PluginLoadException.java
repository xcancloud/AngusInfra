package cloud.xcan.angus.plugin.manager;

/**
 * 当插件无法加载或初始化时抛出。
 *
 * @since 2.0.0
 */
public class PluginLoadException extends RuntimeException {

    /**
     * 创建带有消息的新 {@code PluginLoadException}。
     *
     * @param message 人类可读的失败描述
     */
    public PluginLoadException(String message) {
        super(message);
    }

    /**
     * 创建带有消息和原因的新 {@code PluginLoadException}。
     *
     * @param message 人类可读的失败描述
     * @param cause   底层异常
     */
    public PluginLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
