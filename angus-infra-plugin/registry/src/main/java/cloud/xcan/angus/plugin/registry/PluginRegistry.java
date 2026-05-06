package cloud.xcan.angus.plugin.registry;

import java.util.List;
import java.util.Optional;

/**
 * {@link PluginInfo} 条目的读/写注册表。
 *
 * <p>注册表维护所有当前加载的插件的目录，并
 * 提供按 id、名称和支持平台进行的查找。
 *
 * <p>实现必须是线程安全的。
 *
 * @since 2.0.0
 * @see InMemoryPluginRegistry
 */
public interface PluginRegistry {

    /**
     * 注册插件。如果已存在具有相同 {@code id} 的插件，
     * 则将其替换。
     *
     * @param info 要注册的插件元数据；不能为 {@code null}
     */
    void register(PluginInfo info);

    /**
     * 通过其唯一标识符查找插件。
     *
     * @param id 插件 id；不能为 {@code null}
     * @return 包含匹配的 {@link PluginInfo} 的 {@link Optional}，或空
     */
    Optional<PluginInfo> findById(String id);

    /**
     * 通过其人类可读名称查找插件。
     *
     * @param name 插件名称；不能为 {@code null}
     * @return 包含第一个匹配的 {@link PluginInfo} 的 {@link Optional}，或空
     */
    Optional<PluginInfo> findByName(String name);

    /**
     * 返回所有已注册插件的快照。
     *
     * @return 所有已注册插件的不可修改列表；永不为 {@code null}
     */
    List<PluginInfo> findAll();

    /**
     * 返回所有声明对给定平台名称的支持的插件。
     *
     * @param platform 要过滤的平台名称（例如 {@code "HTTP_TESTING"}）；
     *                 不能为 {@code null}
     * @return 匹配插件的不可修改列表；永不为 {@code null}
     */
    List<PluginInfo> findByPlatform(String platform);

    /**
     * 从注册表中删除具有给定 id 的插件。
     *
     * <p>若未找到指定 id 的插件，则无操作。
     *
     * @param id 要删除的插件 id；不能为 {@code null}
     */
    void unregister(String id);

    /**
     * 返回已注册插件的数量。
     *
     * @return 已注册插件数量
     */
    int size();
}
