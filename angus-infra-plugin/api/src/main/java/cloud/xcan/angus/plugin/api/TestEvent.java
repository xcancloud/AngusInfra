package cloud.xcan.angus.plugin.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 在整个测试生命周期中传递给 {@code ExecutionListener} 回调的事件对象。
 *
 * <p>实例由引擎创建，并传递给每个已注册的监听器。
 * 监听器<em>不得</em>修改事件对象。
 */
public final class TestEvent {

    /**
     * 引擎发出的所有生命周期事件类型的枚举。
     */
    public enum TestEventType {
        /** 整个测试运行启动时触发一次。 */
        TEST_START,
        /** 整个测试运行结束时触发一次（成功或失败）。 */
        TEST_END,
        /** 场景开始执行时触发。 */
        SCENARIO_START,
        /** 场景结束时触发（所有步骤完成或中止）。 */
        SCENARIO_END,
        /** 在步骤的 {@link Sampler#sample(SampleContext)} 调用之前立即触发。 */
        STEP_START,
        /** 在步骤的采样器返回之后立即触发。 */
        STEP_END,
        /** 发生未处理错误时触发（正常断言失败之外的情况）。 */
        ERROR,
        /** 每次负载迭代（虚拟用户循环）开始时触发。 */
        ITERATION_START,
        /** 每次负载迭代结束时触发。 */
        ITERATION_END
    }

    /** 具体的生命周期事件类型。 */
    private final TestEventType type;

    /** 与此事件关联的脚本标识符。 */
    private final String scriptId;

    /** 场景名称（TEST_START / TEST_END 事件可能为 {@code null}）。 */
    private final String scenarioName;

    /** 步骤名称（非步骤事件可能为 {@code null}）。 */
    private final String stepName;

    /** 此事件发出时的挂钟时间戳，单位为自纪元以来的毫秒数。 */
    private final long timestamp;

    /**
     * 与此事件相关的持续时间（毫秒），例如 STEP_END 的步骤响应时间、
     * SCENARIO_END 的场景挂钟时间。开始事件为 {@code 0}。
     */
    private final long duration;

    /**
     * 可扩展的元数据映射。引擎填充标准键；插件可添加自己的键。
     * 引擎标准键示例：{@code "threadId"}、{@code "iteration"}、{@code "statusCode"}。
     */
    private final Map<String, Object> metadata;

    private TestEvent(Builder builder) {
        this.type         = builder.type;
        this.scriptId     = builder.scriptId;
        this.scenarioName = builder.scenarioName;
        this.stepName     = builder.stepName;
        this.timestamp    = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
        this.duration     = builder.duration;
        this.metadata     = builder.metadata != null
                ? new HashMap<>(builder.metadata) : new HashMap<>();
    }

    // ── 访问器 ────────────────────────────────────────────────────────────────

    public TestEventType getType()              { return type; }
    public String getScriptId()                 { return scriptId; }
    public String getScenarioName()             { return scenarioName; }
    public String getStepName()                 { return stepName; }
    public long getTimestamp()                  { return timestamp; }
    public long getDuration()                   { return duration; }
    public Map<String, Object> getMetadata()    { return metadata; }

    /** 获取单个元数据条目的便捷方法。 */
    public Object getMetadata(String key)       { return metadata.get(key); }

    // ── Builder 构建器 ────────────────────────────────────────────────────────

    /** 返回一个新的 {@link Builder} 用于构造 {@link TestEvent}。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link TestEvent} 的流畅构建器。 */
    public static final class Builder {

        private TestEventType type;
        private String scriptId;
        private String scenarioName;
        private String stepName;
        private long timestamp;
        private long duration;
        private Map<String, Object> metadata;

        private Builder() {}

        public Builder type(TestEventType type)                { this.type = type; return this; }
        public Builder scriptId(String scriptId)               { this.scriptId = scriptId; return this; }
        public Builder scenarioName(String scenarioName)       { this.scenarioName = scenarioName; return this; }
        public Builder stepName(String stepName)               { this.stepName = stepName; return this; }
        public Builder timestamp(long timestamp)               { this.timestamp = timestamp; return this; }
        public Builder duration(long duration)                 { this.duration = duration; return this; }
        public Builder metadata(Map<String, Object> metadata)  { this.metadata = metadata; return this; }

        /** 添加单个元数据条目。 */
        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        /** 构建 {@link TestEvent}。 */
        public TestEvent build() {
            return new TestEvent(this);
        }
    }
}
