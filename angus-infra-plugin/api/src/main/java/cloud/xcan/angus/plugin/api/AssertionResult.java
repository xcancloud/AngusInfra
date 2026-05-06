package cloud.xcan.angus.plugin.api;

/**
 * 由采样器或后处理器执行的单个断言评估的不可变结果。
 *
 * <p>实例由采样器（或 {@link PostProcessor}）创建并收集在
 * {@link SampleResult#assertions()} 中。
 *
 * @param name     标识断言规则的人类可读名称
 * @param type     断言类别（例如 {@code "STATUS_CODE"}、{@code "JSON_PATH"}、
 *                 {@code "RESPONSE_TIME"}）
 * @param passed   {@code true} 当断言成功时
 * @param expected 预期值的文本表示（当
 *                 不适用时可能为 {@code null}）
 * @param actual   观察到的实际值的文本表示（可能为 {@code null}）
 * @param message  其他详细信息或失败原因；不需要时为空字符串
 */
public record AssertionResult(
        String name,
        String type,
        boolean passed,
        String expected,
        String actual,
        String message) {

    /**
     * 通过测试的断言的方便工厂。
     *
     * @param name 断言规则名称
     * @param type 断言类别
     * @return 通过的 {@link AssertionResult}，没有预期/实际值
     */
    public static AssertionResult passed(String name, String type) {
        return new AssertionResult(name, type, true, null, null, "");
    }

    /**
     * 失败的断言的方便工厂。
     *
     * @param name     断言规则名称
     * @param type     断言类别
     * @param expected 预期值表示
     * @param actual   实际值表示
     * @param message  人类可读的失败描述
     * @return 失败的 {@link AssertionResult}
     */
    public static AssertionResult failed(String name, String type,
                                         String expected, String actual, String message) {
        return new AssertionResult(name, type, false, expected, actual, message);
    }
}
