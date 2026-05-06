package cloud.xcan.angus.plugin.api;

import cloud.xcan.angus.spec.model.Configuration.TestPlatform;
import java.util.List;

/**
 * 测试执行插件（采样器）的核心 SPI。
 *
 * <p>每个 {@code Sampler} 实现封装特定协议或
 * 技术（HTTP、WebSocket、JDBC、浏览器自动化等）的逻辑。引擎通过 {@link java.util.ServiceLoader} 发现
 * 实现，并根据脚本声明的 {@link TestPlatform} 将脚本步骤路由到正确的插件。
 *
 * <h2>构造函数要求</h2>
 * <p><strong>实现必须提供公共的无参数构造函数。</strong>
 * {@link java.util.ServiceLoader} 需要无参数构造函数来
 * 在运行时实例化插件类。不提供它会导致引擎尝试加载插件时抛出 {@code ServiceConfigurationError}。
 * 繁重初始化应在 {@link #initialize()} 中执行，而不是在构造函数中。
 *
 * <h2>线程安全</h2>
 * <p>{@link #sample(SampleContext)} 可能会从多个虚拟线程并发调用。
 * 实现必须是无状态的或用适当的同步保护可变状态。
 *
 * <p>最小实现示例：
 * <pre>{@code
 * @PluginMetadata(name = "my-http", version = "1.0.0")
 * public class MyHttpSampler extends AbstractSampler {
 *
 *     public MyHttpSampler() {
 *         // ServiceLoader 发现所需的无参数构造函数
 *     }
 *
 *     @Override
 *     public void initialize() {
 *         // 在此处初始化资源（连接池等）
 *     }
 *
 *     @Override
 *     public SampleResult sample(SampleContext ctx) {
 *         // ... 执行 HTTP 调用，返回结果
 *     }
 *
 *     @Override
 *     public List<TestPlatform> getSupportedPlatforms() {
 *         return List.of(TestPlatform.HTTP_TESTING);
 *     }
 * }
 * }</pre>
 *
 * @see AbstractSampler
 */
public interface Sampler {

    /**
     * 执行一个测试样本（一个步骤执行）。
     *
     * <p>此方法在每个虚拟线程迭代中每个步骤调用一次。它不能
     * 返回 {@code null} — 尽可能返回失败的 {@link SampleResult} 而不是
     * 抛出未检查的异常。
     *
     * @param context 包含步骤配置、变量状态、
     *                环境信息和运行时元数据的执行上下文
     * @return 样本结果；永不为 {@code null}
     */
    SampleResult sample(SampleContext context);

    /**
     * 声明此采样器支持的测试平台。
     *
     * <p>引擎使用此列表验证脚本声明的平台是否
     * 由至少一个加载的插件处理，以及在脚本使用时路由步骤
     * {@link TestPlatform#HYBRID} 模式。
     *
     * @return 支持的平台的非空列表
     */
    List<TestPlatform> getSupportedPlatforms();

    /**
     * 返回描述此插件的 {@code step.request} 配置的 JSON Schema 字符串。
     *
     * <p>Schema 由 IDE 工具（例如 VS Code YAML 扩展）表示以进行
     * 自动完成和验证，以及由 {@code angus validate} 进行离线脚本检查。
     *
     * @return 有效的 JSON Schema 字符串，或 {@code null} / 如果未提供则为空字符串
     */
    String getConfigSchema();

    /**
     * 返回此插件的样本脚本模板。
     *
     * <p>模板由 {@code angus init} 呈现给用户，以便他们可以快速
     * 搭建工作脚本，而无需从头开始编写 YAML。
     *
     * @return 模板的列表；如果未提供任何模板则为空列表
     */
    List<ScriptTemplate> getTemplates();

    /**
     * 返回此插件的步骤请求配置的验证器。
     *
     * <p>当非 {@code null} 时，引擎在脚本加载期间调用此验证器
     * 以及在 {@code angus validate} 期间检测执行前的配置错误。
     * 插件特定的验证器定义<em>在</em>插件内部（不在
     * {@code angus-spec} 中）以避免规范模块与插件详细信息的耦合。
     *
     * @return 一个 {@link PluginConfigValidator}，或 {@code null} 如果此插件不提供
     *         执行前验证
     * @since 2.0.0
     */
    default PluginConfigValidator getConfigValidator() {
        return null;
    }

    /**
     * 插件名称/标识符。
     *
     * <p>必须在所有加载的插件中唯一。按照惯例使用小写-连字符
     * (例如 {@code "angus-http"}、{@code "my-mqtt"})。
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 插件版本字符串（推荐 SemVer，例如 {@code "1.2.3"})。
     *
     * @return 插件版本
     */
    String getVersion();

    /**
     * 在插件类加载后立即由引擎调用一次。
     *
     * <p>覆盖以初始化重资源，例如连接池或浏览器
     * 跨所有 {@link #sample(SampleContext)} 调用共享的实例。
     */
    default void initialize() {}

    /**
     * 当插件被卸载或测试运行结束时由引擎调用。
     *
     * <p>实现必须释放在 {@link #initialize()} 中获得的所有资源。
     * 即使测试以错误结束，也保证调用此方法。
     */
    default void destroy() {}
}
