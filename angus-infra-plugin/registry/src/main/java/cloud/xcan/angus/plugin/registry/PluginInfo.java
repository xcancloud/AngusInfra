package cloud.xcan.angus.plugin.registry;

import cloud.xcan.angus.plugin.api.PluginMetadata;
import cloud.xcan.angus.plugin.api.Sampler;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述已注册 AngusMeter 插件的不可变元数据记录。
 *
 * <p>使用 {@link #of(String, String, String, String, List, List, String)} 进行显式构造，
 * 或使用 {@link #fromSampler(Sampler)} 从活跃的 {@link Sampler} 实例中派生元数据。
 *
 * @param id                 唯一插件标识符（按惯例等于 {@code name}）
 * @param name               人类可读的插件名称（如 {@code "angus-http"}）
 * @param version            SemVer 版本字符串（如 {@code "2.0.0"}）
 * @param description        用于 IDE 提示和 {@code angus list} 输出的简短描述
 * @param supportedPlatforms 平台名称列表（如 {@code ["HTTP_TESTING", "HYBRID"]}）
 * @param tags               自由格式搜索标签（如 {@code ["http", "rest"]}）
 * @param configSchema       用于步骤请求验证的 JSON Schema 字符串；
 *                           插件未提供时为空字符串
 * @param status             插件的运行时状态（如 LOADED、FAILED、DISABLED）
 * @since 2.0.0
 */
public record PluginInfo(
        String id,
        String name,
        String version,
        String description,
        List<String> supportedPlatforms,
        List<String> tags,
        String configSchema,
        PluginStatus status
) {

    /** 插件的运行时状态。 */
    public enum PluginStatus {
        /** 插件已加载，可正常使用。 */
        LOADED,
        /** 插件在初始化期间失败。 */
        FAILED,
        /** 插件被配置显式禁用。 */
        DISABLED
    }

    /**
     * 紧凑构造函数——将集合规范化为不可修改的副本。
     */
    public PluginInfo {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(version, "version must not be null");
        description = description != null ? description : "";
        supportedPlatforms = supportedPlatforms != null
                ? List.copyOf(supportedPlatforms) : List.of();
        tags = tags != null ? List.copyOf(tags) : List.of();
        configSchema = configSchema != null ? configSchema : "";
        status = status != null ? status : PluginStatus.LOADED;
    }

    /**
     * 显式构造的工厂方法（不含 schema 的向后兼容重载）。
     *
     * @param id                 唯一插件标识符
     * @param name               人类可读名称
     * @param version            SemVer 版本字符串
     * @param description        简短描述
     * @param supportedPlatforms 平台名称
     * @param tags               搜索标签
     * @return 新的 {@link PluginInfo} 实例，configSchema 为空
     */
    public static PluginInfo of(String id, String name, String version,
                                String description,
                                List<String> supportedPlatforms,
                                List<String> tags) {
        return new PluginInfo(id, name, version, description, supportedPlatforms, tags, "", PluginStatus.LOADED);
    }

    /**
     * 包含 configSchema 的显式构造工厂方法。
     *
     * @param id                 唯一插件标识符
     * @param name               人类可读名称
     * @param version            SemVer 版本字符串
     * @param description        简短描述
     * @param supportedPlatforms 平台名称
     * @param tags               搜索标签
     * @param configSchema       JSON Schema 字符串（可为 {@code null} 或空）
     * @return 新的 {@link PluginInfo} 实例
     */
    public static PluginInfo of(String id, String name, String version,
                                String description,
                                List<String> supportedPlatforms,
                                List<String> tags,
                                String configSchema) {
        return new PluginInfo(id, name, version, description, supportedPlatforms, tags, configSchema, PluginStatus.LOADED);
    }

    /**
     * 从活跃的 {@link Sampler} 实例派生 {@link PluginInfo}。
     *
     * <p>采样器的 {@link Sampler#getName()} 同时用于 {@code id} 和 {@code name}。
     * {@code description} 在 {@link PluginMetadata} 注解存在时从其读取。
     * 插件的 JSON Schema 通过 {@link Sampler#getConfigSchema()} 获取。
     *
     * @param sampler 要派生元数据的采样器；不能为 {@code null}
     * @return 表示给定采样器的 {@link PluginInfo}
     */
    public static PluginInfo fromSampler(Sampler sampler) {
        Objects.requireNonNull(sampler, "sampler must not be null");
        List<String> platforms = sampler.getSupportedPlatforms().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        String desc = "";
        List<String> tags = List.of();
        PluginMetadata meta = sampler.getClass().getAnnotation(PluginMetadata.class);
        if (meta != null) {
            if (!meta.description().isEmpty()) {
                desc = meta.description();
            }
            if (meta.tags().length > 0) {
                tags = List.of(meta.tags());
            }
        }
        String schema = sampler.getConfigSchema();
        return new PluginInfo(
                sampler.getName(),
                sampler.getName(),
                sampler.getVersion(),
                desc,
                platforms,
                tags,
                schema != null ? schema : "",
                PluginStatus.LOADED
        );
    }
}
