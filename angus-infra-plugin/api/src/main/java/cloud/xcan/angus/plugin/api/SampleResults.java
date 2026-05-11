package cloud.xcan.angus.plugin.api;

import java.util.List;
import java.util.Locale;

/**
 * {@link SampleResult} 工具类：在编排器边界统一回填失败语义元数据。
 *
 * <p>各插件 Sampler 仅负责产出基础 {@link SampleResult}（含 {@code success}、{@code errorCode}、
 * {@code assertions}）；本类负责根据这些字段推导出 {@link SampleResult.FailureKind}
 * 以及首个失败断言的详情，供指标聚合、错误报表、重试策略统一消费。
 *
 * <p>设计原则：
 * <ul>
 *   <li>不修改样本字段语义；仅补充派生字段。</li>
 *   <li>幂等：对已包含 failureKind 的样本不再覆盖。</li>
 *   <li>无副作用：返回新的不可变 {@link SampleResult} 实例。</li>
 * </ul>
 *
 * @since 2.0.0
 */
public final class SampleResults {

    private SampleResults() {}

    /**
     * 根据 {@link SampleResult#getAssertions()} / {@link SampleResult#getErrorCode()} /
     * {@link SampleResult#isSuccess()} 推导失败语义元数据并写入新的 {@link SampleResult}。
     *
     * <p>派生规则：
     * <ol>
     *   <li>样本成功 ⇒ failureKind = {@code null}，其它失败元数据均为 {@code null}。</li>
     *   <li>断言中存在 {@code passed=false} ⇒
     *       failureKind = {@code ASSERTION_FAILED}，回填首个失败断言的 name/expected/actual。</li>
     *   <li>errorCode 包含 {@code TIMEOUT} ⇒ failureKind = {@code TIMEOUT}。</li>
     *   <li>其他失败 ⇒ failureKind = {@code TRANSPORT_ERROR}。</li>
     * </ol>
     *
     * <p>若样本已携带 {@code failureKind}（来自上游显式标注），则保持原值不覆盖。
     *
     * @param result 原始样本结果；{@code null} 时原样返回
     * @return 含失败语义元数据的新样本；如无需补充则返回原对象
     */
    public static SampleResult enrichFailureMeta(SampleResult result) {
        if (result == null) {
            return null;
        }
        if (result.getFailureKind() != null) {
            return result;
        }
        if (result.isSuccess()) {
            return result;
        }

        // 1) 优先识别断言失败：若 assertions 中存在 passed=false，归为 ASSERTION_FAILED
        List<AssertionResult> assertions = result.getAssertions();
        if (assertions != null) {
            for (AssertionResult a : assertions) {
                if (!a.passed()) {
                    return result.toBuilder()
                            .failureKind(SampleResult.FailureKind.ASSERTION_FAILED)
                            .failedAssertionName(a.name())
                            .failedAssertionExpected(a.expected())
                            .failedAssertionActual(a.actual())
                            .build();
                }
            }
        }

        // 2) 通过 errorCode 区分 TIMEOUT vs TRANSPORT_ERROR
        SampleResult.FailureKind kind = classifyByErrorCode(result.getErrorCode());
        return result.toBuilder().failureKind(kind).build();
    }

    /**
     * 仅根据 {@code errorCode} 推断失败类型；未匹配关键字时归为 {@code TRANSPORT_ERROR}。
     */
    private static SampleResult.FailureKind classifyByErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) {
            return SampleResult.FailureKind.TRANSPORT_ERROR;
        }
        String code = errorCode.toUpperCase(Locale.ROOT);
        if (code.contains("TIMEOUT") || code.contains("TIME_OUT")) {
            return SampleResult.FailureKind.TIMEOUT;
        }
        if (code.equals("SCRIPT_ERROR") || code.equals("DATA_SOURCE_ERROR")
                || code.equals("NO_DATA_SOURCE_PROVIDER")) {
            return SampleResult.FailureKind.SCRIPT_ERROR;
        }
        return SampleResult.FailureKind.TRANSPORT_ERROR;
    }
}
