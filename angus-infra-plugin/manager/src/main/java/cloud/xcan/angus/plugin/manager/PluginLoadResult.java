package cloud.xcan.angus.plugin.manager;

import java.util.List;

/**
 * 插件加载操作的结果，包含计数和详细的失败信息。
 *
 * @param loaded   成功加载的插件数量
 * @param failed   加载失败的插件数量
 * @param failures 每个失败插件对应的异常列表
 * @since 2.0.0
 */
public record PluginLoadResult(int loaded, int failed, List<PluginLoadException> failures) {

    /**
     * 创建新的 PluginLoadResult。
     *
     * @param loaded   成功加载的插件数量
     * @param failed   加载失败的插件数量
     * @param failures 每个失败插件对应的异常列表；不能为 null
     */
    public PluginLoadResult {
        failures = List.copyOf(failures);
    }

    /**
     * 若所有插件均成功加载则返回 true。
     *
     * @return 无失败时返回 true
     */
    public boolean isSuccess() {
        return failed == 0;
    }

    /**
     * 若至少有一个插件加载失败则返回 true。
     *
     * @return 有任何失败时返回 true
     */
    public boolean hasFailures() {
        return failed > 0;
    }
}
