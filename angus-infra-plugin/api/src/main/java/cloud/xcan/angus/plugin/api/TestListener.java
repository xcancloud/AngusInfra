package cloud.xcan.angus.plugin.api;

/**
 * 测试生命周期事件侦听器的 SPI。
 *
 * @deprecated 改用 {@code cloud.xcan.angus.core.engine.ExecutionListener}，
 *             它现在通过默认方法 {@code onScenarioStart}、{@code onScenarioEnd}、
 *             {@code onStepStart}、{@code onStepEnd} 包括场景级和步骤级回调。
 *             此接口将在未来版本中删除。
 * @since 2.0.0
 */
@Deprecated(since = "2.1.0", forRemoval = true)
public interface TestListener {

    /**
     * 在整个测试运行启动时调用一次。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#TEST_START} 的事件
     */
    void onTestStart(TestEvent event);

    /**
     * 在整个测试运行结束时调用一次（无论成功还是失败）。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#TEST_END} 的事件
     */
    void onTestEnd(TestEvent event);

    /**
     * 在场景开始执行时调用。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#SCENARIO_START} 的事件
     */
    void onScenarioStart(TestEvent event);

    /**
     * 在场景完成时调用（所有步骤完成或中止）。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#SCENARIO_END} 的事件
     */
    void onScenarioEnd(TestEvent event);

    /**
     * 在步骤的采样器被调用之前立即调用。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#STEP_START} 的事件
     */
    void onStepStart(TestEvent event);

    /**
     * 在步骤的采样器返回后立即调用。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#STEP_END} 的事件
     */
    void onStepEnd(TestEvent event);

    /**
     * 当发生未处理的错误时调用（例如来自未返回失败 {@link SampleResult} 的采样器的未捕获异常）。
     *
     * @param event 类型为 {@link TestEvent.TestEventType#ERROR} 的事件
     * @param error 抛出的错误；永不为 {@code null}
     */
    void onError(TestEvent event, Throwable error);

    /**
     * 用于日志和诊断的此侦听器的人类可读名称。
     *
     * <p>如果未覆盖，默认为简单类名。
     *
     * @return 侦听器名称
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
