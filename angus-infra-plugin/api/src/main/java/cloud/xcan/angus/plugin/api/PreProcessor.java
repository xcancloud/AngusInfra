package cloud.xcan.angus.plugin.api;

/**
 * 在采样器执行<em>之前</em>运行的请求预处理器的 SPI。
 *
 * <p>预处理器可以修改 {@link SampleContext}（例如注入 HMAC 签名
 * 进入 {@link SampleContext#getStepConfig()}、编码请求体、解决动态
 * 变量），然后引擎将上下文传递给 {@link Sampler}。
 *
 * <p>可能会注册多个预处理器；引擎按声明
 * 顺序调用它们。预处理器通过 {@link java.util.ServiceLoader} 发现。
 *
 * <p>实现必须是线程安全的（可能会并发调用）。
 */
@FunctionalInterface
public interface PreProcessor {

    /**
     * 在采样器运行之前处理（并可选地变更）执行上下文。
     *
     * @param context 当前步骤的可变执行上下文；永不为 {@code null}
     */
    void process(SampleContext context);

    /**
     * 用于日志和诊断的此预处理器的人类可读名称。
     *
     * <p>如果未覆盖，默认为简单类名。
     *
     * @return 预处理器名称
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 此预处理器执行内容的简短描述。
     *
     * @return 描述字符串
     */
    default String getDescription() {
        return "";
    }
}
