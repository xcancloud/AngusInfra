package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.spec.model.TestType;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 为每个步骤执行传递给 {@link Sampler} 的可变执行上下文。
 *
 * <p>引擎在调用 {@link Sampler#sample(SampleContext)} 之前
 * 填充 {@code SampleContext}（含 {@code scriptId}、{@code debug}、{@code testType} 等）。
 * 采样器可以读取所有字段，并可以写入 {@link #variables} 和 {@link #properties}，
 * 以便下游步骤或后处理器可以使用提取的值。
 *
 * <p>实例<em>不是</em>线程安全的；每个虚拟线程接收自己的副本。
 */
public final class SampleContext {

    /** 脚本标识符（来自加载的 {@code AngusScript}）。 */
    private final String scriptId;

    /** 当前执行的场景的名称。 */
    private final String scenarioName;

    /** 当前执行的步骤的名称。 */
    private final String stepName;

    /**
     * 从 YAML 反序列化为通用映射的原始步骤请求配置。
     * 采样器实现将其强制转换/反序列化为其自己的配置模型。
     */
    private final Map<String, Object> stepConfig;

    /** 当前变量值（在同一场景中的步骤之间共享）。 */
    private final Map<String, Object> variables;

    /** 活动环境的名称（例如 {@code "staging"}、{@code "prod"}）。 */
    private final String environment;

    /**
     * 为活动环境解析的环境变量和标头。
     * 键是变量/标头名称；值是其解析的字符串值。
     */
    private final Map<String, String> environmentConfig;

    /** 当前迭代索引（从 1 开始）。 */
    private final int iteration;

    /** 此运行中的虚拟线程/工作程序索引（从 1 开始）。 */
    private final int threadId;

    /** 此步骤执行的挂钟开始时间（自纪元以来的毫秒）。 */
    private final long startTime;

    /**
     * 用于不属于
     * 标准上下文字段的插件特定属性的可扩展映射。采样器、预处理器和后处理器
     * 可以自由读取和写入此处。
     */
    private final Map<String, Object> properties;

    /**
     * 此子步骤允许的最大执行时间。
     * {@code null} 或一个非正整数执行时间意味着引擎不强制超时。
     * 通过 {@link Builder} 构造时默认为 {@code Duration.ofSeconds(60)}。
     */
    private final Duration stepTimeout;

    /**
     * 可选的步骤认证配置。存在时，映射通常
     * 包含 {@code "type"}(类型)、{@code "username"}(用户名)、
     * {@code "password"}(密码)、{@code "token"}(令牌)等键。
     * 需要认证的插件应从此读取，而不是手动解析 {@link #stepConfig}。
     *
     * <p>映射的类型为 {@code Map<String, Object>}，故 {@code plugin-api}
     * 不依赖 {@code runtime-auth} 模块。
     */
    private final Map<String, Object> authConfig;

    /**
     * 与 {@code AngusScript.configuration.debug} 对齐：为 {@code true} 时插件可输出
     * 完整诊断元数据（如 HTTP 的 curl/HAR、JDBC 的 SQL 细节等）。由引擎在构建上下文时设置。
     */
    private final boolean debug;

    /**
     * 与 {@code AngusScript.configuration.testType} 对齐的测试意图类型，供插件按
     * FUNCTIONAL / PERFORMANCE / SECURITY 调整采样粒度或上报形态。
     */
    private final TestType testType;

    private SampleContext(Builder builder) {
        this.scriptId          = builder.scriptId;
        this.scenarioName      = builder.scenarioName;
        this.stepName          = builder.stepName;
        this.stepConfig        = builder.stepConfig != null
                ? new HashMap<>(builder.stepConfig) : new HashMap<>();
        this.variables         = builder.variables != null
                ? builder.variables : new HashMap<>();
        this.environment       = builder.environment;
        this.environmentConfig = builder.environmentConfig != null
                ? new HashMap<>(builder.environmentConfig) : new HashMap<>();
        this.iteration         = builder.iteration;
        this.threadId          = builder.threadId;
        this.startTime         = builder.startTime;
        this.properties        = builder.properties != null
                ? builder.properties : new HashMap<>();
        this.stepTimeout       = builder.stepTimeout;
        this.authConfig        = builder.authConfig != null
                ? new HashMap<>(builder.authConfig) : null;
        this.debug = builder.debug;
        this.testType = builder.testType != null ? builder.testType : TestType.PERFORMANCE;
    }

    // ==================== 访问器 ====================

    public String getScriptId()                        { return scriptId; }
    public String getScenarioName()                    { return scenarioName; }
    public String getStepName()                        { return stepName; }
    public Map<String, Object> getStepConfig()         { return stepConfig; }
    public Map<String, Object> getVariables()          { return variables; }
    public String getEnvironment()                     { return environment; }
    public Map<String, String> getEnvironmentConfig()  { return environmentConfig; }
    public int getIteration()                          { return iteration; }
    public int getThreadId()                           { return threadId; }
    public long getStartTime()                         { return startTime; }
    public Map<String, Object> getProperties()         { return properties; }
    public Duration getStepTimeout()                   { return stepTimeout; }
    public Map<String, Object> getAuthConfig()         { return authConfig; }
    /** 是否处于脚本级调试模式（{@code configuration.debug}）。 */
    public boolean isDebug()                               { return debug; }
    /** 脚本级 {@code configuration.testType} 的运行时镜像（未配置时为 {@link TestType#PERFORMANCE}）。 */
    public TestType getTestType()                          { return testType; }

    // ==================== 便利变量加乱器 ====================

    /**
     * 返回已命名变量的当前值，或如果不存在则返回 {@code null}。
     *
     * @param name 变量名称
     * @return 当前值或 {@code null}
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }

    /**
     * 设置变量值，使其对同一场景中的后续步骤亟是。
     *
     * @param name  变量名称（不能为 {@code null}）
     * @param value 新值（可以为 {@code null} 以清除）
     */
    public void setVariable(String name, Object value) {
        Objects.requireNonNull(name, "Variable name must not be null");
        variables.put(name, value);
    }

    // ==================== 便利属性加乱器 ====================

    /**
     * 返回插件特定属性，或如果不存在则返回 {@code null}。
     *
     * @param key 属性键
     * @return 当前值或 {@code null}
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * 设置插件特定属性。
     *
     * @param key   属性键（不能为 {@code null}）
     * @param value 属性值（可以为 {@code null} 以清除）
     */
    public void setProperty(String key, Object value) {
        Objects.requireNonNull(key, "Property key must not be null");
        properties.put(key, value);
    }

    // ==================== 构建器 ====================

    /** 返回一个新的 {@link Builder} 用于构造 {@link SampleContext}。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link SampleContext} 的流畅构建器。 */
    public static final class Builder {

        private String scriptId;
        private String scenarioName;
        private String stepName;
        private Map<String, Object> stepConfig;
        private Map<String, Object> variables;
        private String environment;
        private Map<String, String> environmentConfig;
        private int iteration = 1;
        private int threadId  = 1;
        private long startTime = System.currentTimeMillis();
        private Map<String, Object> properties;
        /**
         * 默认步骤超时时间为 60 秒。设置为 {@code null} 或 {@link Duration#ZERO}
         * 以禁用特定步骤的超时强制。
         */
        private Duration stepTimeout = Duration.ofSeconds(60);
        private Map<String, Object> authConfig;
        private boolean debug;
        private TestType testType = TestType.PERFORMANCE;

        private Builder() {}

        public Builder scriptId(String scriptId)                               { this.scriptId = scriptId; return this; }
        public Builder scenarioName(String scenarioName)                       { this.scenarioName = scenarioName; return this; }
        public Builder stepName(String stepName)                               { this.stepName = stepName; return this; }
        public Builder stepConfig(Map<String, Object> stepConfig)              { this.stepConfig = stepConfig; return this; }
        public Builder variables(Map<String, Object> variables)                { this.variables = variables; return this; }
        public Builder environment(String environment)                         { this.environment = environment; return this; }
        public Builder environmentConfig(Map<String, String> environmentConfig){ this.environmentConfig = environmentConfig; return this; }
        public Builder iteration(int iteration)                                { this.iteration = iteration; return this; }
        public Builder threadId(int threadId)                                  { this.threadId = threadId; return this; }
        public Builder startTime(long startTime)                               { this.startTime = startTime; return this; }
        public Builder properties(Map<String, Object> properties)              { this.properties = properties; return this; }
        public Builder stepTimeout(Duration stepTimeout)                       { this.stepTimeout = stepTimeout; return this; }
        public Builder authConfig(Map<String, Object> authConfig)              { this.authConfig = authConfig; return this; }
        /** 脚本级 {@code configuration.debug} 的运行时镜像。 */
        public Builder debug(boolean debug)                                  { this.debug = debug; return this; }
        /** 脚本级 {@code configuration.testType}；{@code null} 视为 {@link TestType#PERFORMANCE}。 */
        public Builder testType(TestType testType) {
            this.testType = testType != null ? testType : TestType.PERFORMANCE;
            return this;
        }

        /** 构造不可变 {@link SampleContext}。 */
        public SampleContext build() {
            return new SampleContext(this);
        }
    }
}
