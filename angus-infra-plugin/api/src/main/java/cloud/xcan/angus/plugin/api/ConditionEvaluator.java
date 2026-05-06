package cloud.xcan.angus.plugin.api;

import java.util.Map;

/**
 * 对条件分支和 while 循环守卫中使用的布尔条件表达式进行求值。
 *
 * <p>实现至少应支持：
 * <ul>
 *   <li>字面布尔值：{@code "true"}、{@code "false"}</li>
 *   <li>相等/不等：{@code "${var} == value"}、{@code "${var} != value"}</li>
 *   <li>真值检查：{@code "${var}"}（非 null、非空、非 "false"）</li>
 * </ul>
 *
 * <p>默认实现位于 {@code runtime-expression} 模块
 * （{@code DefaultConditionEvaluator}），委托给表达式引擎进行
 * 更丰富的求值。当没有可用的表达式引擎时，引擎也可以
 * 回退到简单的内置求值器。
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface ConditionEvaluator {

    /**
     * 根据变量映射对给定表达式求值。
     *
     * @param expression 布尔表达式（可包含 {@code ${variable}} 引用）
     * @param variables  当前变量值
     * @return 若表达式求值为真值则返回 {@code true}
     */
    boolean evaluate(String expression, Map<String, Object> variables);
}
