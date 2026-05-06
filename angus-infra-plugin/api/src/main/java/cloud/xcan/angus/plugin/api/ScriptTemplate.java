package cloud.xcan.angus.plugin.api;

/**
 * 由 {@link Sampler#getTemplates()} 返回的不可变 YAML 脚本模板。
 *
 * <p>模板由 {@code angus init} CLI 命令使用，用于为特定插件快速生成新的测试脚本。
 *
 * @param name        在 CLI 选择菜单中显示的简短名称
 * @param description 说明模板用途的较长描述
 * @param category    分组类别（如 {@code "basic"}、{@code "auth"}、{@code "perf"}）
 * @param yamlContent 完整的、可直接使用的 YAML 脚本内容
 */
public record ScriptTemplate(
        String name,
        String description,
        String category,
        String yamlContent) {

    /**
     * 使用默认的 {@code "basic"} 类别的便捷工厂方法。
     *
     * @param name        模板显示名称
     * @param description 模板描述
     * @param yamlContent YAML 脚本内容
     * @return 新的 {@link ScriptTemplate} 实例
     */
    public static ScriptTemplate of(String name, String description, String yamlContent) {
        return new ScriptTemplate(name, description, "basic", yamlContent);
    }

    /**
     * 用于快速创建模板的最简便捷工厂方法（描述默认为 name，类别默认为 {@code "basic"}）。
     *
     * @param name        模板显示名称
     * @param yamlContent YAML 脚本内容
     * @return 新的 {@link ScriptTemplate} 实例
     */
    public static ScriptTemplate of(String name, String yamlContent) {
        return new ScriptTemplate(name, name, "basic", yamlContent);
    }
}
