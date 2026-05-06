package cloud.xcan.angus.plugin.registry;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 {@link ConcurrentHashMap} 的线程安全 {@link PluginRegistry} 实现。
 *
 * <p>所有读操作返回不可修改的快照，调用者无法改变内部状态。
 * 写操作（{@link #register} 和 {@link #unregister}）相对于底层映射是原子的。
 *
 * @since 2.0.0
 */
public class InMemoryPluginRegistry implements PluginRegistry {

    private final ConcurrentHashMap<String, PluginInfo> store = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void register(PluginInfo info) {
        Objects.requireNonNull(info, "info must not be null");
        store.put(info.id(), info);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PluginInfo> findById(String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(store.get(id));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PluginInfo> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return store.values().stream()
                .filter(p -> name.equals(p.name()))
                .findFirst();
    }

    /** {@inheritDoc} */
    @Override
    public List<PluginInfo> findAll() {
        return List.copyOf(store.values());
    }

    /** {@inheritDoc} */
    @Override
    public List<PluginInfo> findByPlatform(String platform) {
        Objects.requireNonNull(platform, "platform must not be null");
        return store.values().stream()
                .filter(p -> p.supportedPlatforms().contains(platform))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public void unregister(String id) {
        Objects.requireNonNull(id, "id must not be null");
        store.remove(id);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return store.size();
    }
}
