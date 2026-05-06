package cloud.xcan.angus.plugin.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * {@link Sampler} 实现的方便抽象基类。
 *
 * <p>为除 {@link #sample(SampleContext)}、{@link #getSupportedPlatforms()}、
 * {@link #getName()} 和 {@link #getVersion()} 外的所有方法提供合理的默认实现。
 * 子类可以覆盖任何方法。
 *
 * <h2>构造函数要求</h2>
 * <p><strong>子类必须提供公共的无参数构造函数</strong>（或依赖
 * 隐式默认构造函数）以使 {@link java.util.ServiceLoader} 发现工作正常。
 * 无参数构造函数调用 {@code super()}，该方法从
 * {@link PluginMetadata} 注解中读取元数据。如果子类需要构造函数参数，
 * 改用 {@link Sampler#initialize()} 进行设置。
 *
 * <p>插件名称和版本是从
 * 子类上的 {@link PluginMetadata} 注解在构造时读取的，因此
 * 带注解的子类无需覆盖那些方法：
 *
 * <pre>{@code
 * @PluginMetadata(name = "my-http", version = "1.0.0", description = "Custom HTTP sampler")
 * public class MyHttpSampler extends AbstractSampler {
 *
 *     // 编译器提供的隐式无参数构造函数调用 super()
 *
 *     @Override
 *     public void initialize() {
 *         // 在此设置资源（连接池、浏览器实例等）
 *     }
 *
 *     @Override
 *     public SampleResult sample(SampleContext ctx) { ... }
 *
 *     @Override
 *     public List<TestPlatform> getSupportedPlatforms() {
 *         return List.of(TestPlatform.HTTP_TESTING);
 *     }
 * }
 * }</pre>
 *
 * <h3>基于约定的模式加载</h3>
 *
 * <p>如果子类不覆盖 {@link #getConfigSchema()}，此基类
 * 将尝试从类路径资源加载 JSON 模式：
 * <pre>
 *   META-INF/angus-plugins/{plugin-name}/schema.json
 * </pre>
 * 其中 {@code {plugin-name}} 是 {@link #getName()} 返回的值。如果
 * 找不到资源，该方法返回空字符串（与之前相同）。此
 * 约定将插件特定的模式保留在每个插件模块<em>内部</em>，避免
 * 与 {@code angus-spec} 的耦合。
 *
 * @see Sampler
 */
public abstract class AbstractSampler implements Sampler {

    /** 预期找到插件特定资源的类路径前缀。 */
    private static final String RESOURCE_PREFIX = "META-INF/angus-plugins/";

    private final String name;
    private final String version;

    /**
     * 从本类及其父类链上读取 {@link PluginMetadata} 以初始化
     * {@link #getName()} 和 {@link #getVersion()}（便于测试子类或未使用
     * {@code @Inherited} 时仍继承父类上的注解）。当注解不存在时
     * 退会到简单类名和 {@code "0.0.0"}。
     */
    protected AbstractSampler() {
        PluginMetadata meta = resolvePluginMetadata(getClass());
        if (meta != null) {
            this.name    = meta.name();
            this.version = meta.version();
        } else {
            this.name    = getClass().getSimpleName();
            this.version = "0.0.0";
        }
    }

    /**
     * 从本类沿继承链向上查找 {@link PluginMetadata}（注解未标记 {@code @Inherited} 时，
     * 子类需能继承父类上的声明）。
     */
    private static PluginMetadata resolvePluginMetadata(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            PluginMetadata meta = c.getAnnotation(PluginMetadata.class);
            if (meta != null) {
                return meta;
            }
        }
        return null;
    }

    // ==================== Sampler 默认实现 ====================

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * 返回插件的 JSON Schema 以执行步骤请求验证。
     *
     * <p>默认情况下，尝试从类路径资源加载 schema
     * {@code META-INF/angus-plugins/{name}/schema.json}。如果资源不
     * 存在，返回空字符串。子类可以覆盖以从另一个源提供 schema
     * （例如静态常量或编程构建器）。
     *
     * @return JSON Schema 字符串，或空字符串如果没有可用的
     */
    @Override
    public String getConfigSchema() {
        return loadClasspathResource(RESOURCE_PREFIX + name + "/schema.json");
    }

    /**
     * 默认情况下返回空模板列表。
     *
     * <p>覆盖以为此插件提供 {@code angus init} 模板。
     *
     * @return 空不可修改列表
     */
    @Override
    public List<ScriptTemplate> getTemplates() {
        return Collections.emptyList();
    }

    // ==================== 子类的实用方法 ====================

    /**
     * 创建一个最小化的成功 {@link SampleResult}，仅预设启动时间。
     *
     * <p>子类应该填充剩余字段（响应体、状态代码等）
     * 后再返回。
     *
     * @return 新构建器，其中 {@link SampleResult.Builder#startTime(long)} 设置为现在
     */
    protected SampleResult.Builder createResult() {
        return SampleResult.builder().startTime(System.currentTimeMillis());
    }

    /**
     * 创建一个预填充的 <em>失败</em> {@link SampleResult}。
     *
     * <p>开始/结束时间都设置为当前时刻，
     * 且 {@link SampleResult#isSuccess()} 将返回 {@code false}。
     *
     * @param message 人类可读的失败描述
     * @param cause   导致失败的异常，或 {@code null}
     * @return 完整构建的失败结果
     */
    protected SampleResult createFailedResult(String message, Throwable cause) {
        long now = System.currentTimeMillis();
        String errorCode = cause != null ? cause.getClass().getSimpleName() : "ERROR";
        return SampleResult.builder()
                .success(false)
                .startTime(now)
                .endTime(now)
                .errorMessage(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * 将类路径资源加载为 UTF-8 字符串。
     *
     * @param path 资源路径（例如 {@code "META-INF/angus-plugins/angus-http/schema.json"}）
     * @return 资源内容，或空字符串如果找不到资源
     */
    protected String loadClasspathResource(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
