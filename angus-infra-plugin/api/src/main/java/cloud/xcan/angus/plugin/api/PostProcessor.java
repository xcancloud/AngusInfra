package cloud.xcan.angus.plugin.api;

/**
 * 在采样器返回<em>之后</em>运行的响应后处理器的 SPI。
 *
 * <p>后处理器可以检查或转换 {@link SampleResult}（例如解密
 * 加密的响应体、运行自定义断言、提取其他变量或缓存
 * 响应数据以供下游步骤使用）。
 *
 * <p>可能会注册多个后处理器；引擎按声明
 * 顺序调用它们。后处理器通过 {@link java.util.ServiceLoader} 发现。
 *
 * <p>实现必须是线程安全的（可能会并发调用）。
 */
@FunctionalInterface
public interface PostProcessor {

    /**
     * 采样器返回后处理样本结果。
     *
     * <p>实现可能会变更 {@code 结果}（例如追加 {@link AssertionResult}
     * 条目或填充 {@link SampleResult#getExtractedVariables()}）并且也可能
     * 通过 {@link SampleContext#setVariable(String, Object)} 将提取的值写回到 {@code context}。
     *
     * @param context 完成的步骤的执行上下文；永不为 {@code null}
     * @param result  采样器生成的结果；永不为 {@code null}
     */
    void process(SampleContext context, SampleResult result);

    /**
     * 用于日志和诊断的此后处理器的人类可读名称。
     *
     * <p>如果未覆盖，默认为简单类名。
     *
     * @return 后处理器名称
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
