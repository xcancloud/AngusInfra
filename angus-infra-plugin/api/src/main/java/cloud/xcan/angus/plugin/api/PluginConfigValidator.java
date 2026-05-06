package cloud.xcan.angus.plugin.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 插件特定步骤配置验证的 SPI。
 *
 * <p>每个插件可以提供一个 {@code PluginConfigValidator}，用于在执行<em>之前</em>
 * 验证原始步骤 {@code request} 映射。这使得插件可以在脚本加载或
 * {@code angus validate} 阶段尽早报告配置错误，而不是在
 * {@link Sampler#sample(SampleContext)} 运行时才失败。
 *
 * <p>验证器刻意定义在插件本身中——<strong>而非</strong>在 {@code angus-spec} 中——
 * 以使规范模块与插件特定的配置细节解耦。引擎通过
 * {@link Sampler#getConfigValidator()} 发现验证器。
 *
 * <p>示例实现：
 * <pre>{@code
 * public class HttpConfigValidator implements PluginConfigValidator {
 *
 *     @Override
 *     public List<String> validate(Map<String, Object> stepConfig) {
 *         List<String> errors = new ArrayList<>();
 *         if (!stepConfig.containsKey("method")) {
 *             errors.add("Missing required field: method");
 *         }
 *         if (!stepConfig.containsKey("url")) {
 *             errors.add("Missing required field: url");
 *         }
 *         return errors;
 *     }
 * }
 * }</pre>
 *
 * @since 2.0.0
 * @see Sampler#getConfigValidator()
 */
@FunctionalInterface
public interface PluginConfigValidator {

    /**
     * 验证步骤的原始请求配置映射。
     *
     * <p>该映射对应 YAML 脚本中步骤的 {@code request:} 块。
     * 实现应检查必填键、有效值类型、枚举成员资格
     * 以及任何其他插件特定的约束。
     *
     * @param stepConfig 原始步骤请求配置；永不为 {@code null}
     * @return 人类可读错误消息列表；配置有效时为空列表
     */
    List<String> validate(Map<String, Object> stepConfig);

    /**
     * 返回非阻塞的警告信息（不会阻止执行，但应提示给用户）。
     *
     * <p>默认返回空列表，向后兼容历史实现。插件可覆盖此方法将建议性
     * 提示与真正的错误分离。
     *
     * @param stepConfig 原始步骤请求配置
     * @return 人类可读警告消息列表
     * @since 2.0.0
     */
    default List<String> getWarnings(Map<String, Object> stepConfig) {
        return Collections.emptyList();
    }
}
