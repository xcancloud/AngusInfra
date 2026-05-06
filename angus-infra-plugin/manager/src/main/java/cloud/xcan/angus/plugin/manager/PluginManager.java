package cloud.xcan.angus.plugin.manager;

import cloud.xcan.angus.core.engine.PluginRouter;
import cloud.xcan.angus.plugin.api.PostProcessor;
import cloud.xcan.angus.plugin.api.PreProcessor;
import cloud.xcan.angus.plugin.api.Sampler;
import cloud.xcan.angus.plugin.registry.PluginRegistry;
import java.util.List;
import java.util.Optional;

/**
 * 管理 {@link Sampler} 插件的完整生命周期：发现、注册、
 * 查找和 {@link PluginRouter} 构造。
 *
 * <p>默认实现（{@link AngusPluginManager}）通过
 * {@link java.util.ServiceLoader} 发现插件。对于测试或嵌入式场景，
 * 插件也可通过 {@link #registerSampler(Sampler)} 显式注册。
 *
 * <p>实现必须是线程安全的。
 *
 * @since 2.0.0
 * @see AngusPluginManager
 */
public interface PluginManager {

    /**
     * 通过 {@link java.util.ServiceLoader} 发现并注册类路径上所有
     * {@link Sampler} 实现。
     *
     * <p>对于每个发现的采样器，{@link Sampler#initialize()} 会被调用一次，
     * 且采样器的元数据会注册到 {@link PluginRegistry}。
     *
     * @deprecated 改用 {@link #loadPluginsWithResult()} 以获取详细的失败信息。
     */
    @Deprecated(since = "2.0.0", forRemoval = false)
    void loadPlugins();

    /**
     * 通过 {@link java.util.ServiceLoader} 发现并注册类路径上所有
     * {@link Sampler} 实现，并返回详细结果。
     *
     * <p>对于每个发现的采样器，{@link Sampler#initialize()} 会被调用一次，
     * 且采样器的元数据会注册到 {@link PluginRegistry}。
     * 与 {@link #loadPlugins()} 不同，此方法会返回任何加载失败的插件信息。
     *
     * @return 包含计数和失败详情的 {@link PluginLoadResult}
     * @since 2.0.0
     */
    PluginLoadResult loadPluginsWithResult();

    /**
     * 从外部目录加载 jar 形式的插件。
     *
     * <p>读取 {@link PluginsConfig#dir()}（为空则回退 {@code defaultDir}），按
     * {@link PluginsConfig#classLoaderStrategy()} 装载该目录下的所有
     * {@code *.jar}。同名 sampler 会按 {@link #registerSampler(Sampler)} 的语义被覆盖。
     *
     * <p>当 {@link PluginsConfig#failOnError()} 为 {@code true} 且本次加载存在失败时，
     * 实现可抛出 {@link IllegalStateException} 以中止启动；否则失败信息会通过
     * 返回的 {@link PluginLoadResult#failures()} 暴露。
     *
     * @param pc         插件目录配置；不能为 {@code null}
     * @param defaultDir 当 {@code pc.dir()} 为空时使用的默认目录；可为 {@code null}
     * @return 本次目录扫描的加载结果；目录不存在或为空时返回全 0 的结果
     * @since 2.0.0
     */
    PluginLoadResult loadExternalPlugins(PluginsConfig pc, String defaultDir);

    /**
     * 显式注册单个采样器，无需服务发现。
     *
     * <p>{@link Sampler#initialize()} 会立即被调用。
     *
     * @param sampler 要注册的采样器；不能为 {@code null}
     */
    void registerSampler(Sampler sampler);

    /**
     * 返回以给定名称注册的采样器。
     *
     * @param name 采样器名称（参见 {@link Sampler#getName()}）；不能为 {@code null}
     * @return 包含采样器的 {@link Optional}，若未找到则为空
     */
    Optional<Sampler> getSampler(String name);

    /**
     * 返回所有当前注册的采样器。
     *
     * @return 不可修改的采样器列表；永不为 {@code null}
     */
    List<Sampler> getAllSamplers();

    /**
     * 从所有当前注册的采样器构造一个 {@link PluginRouter}。
     *
     * @return 新的 {@link PluginRouter} 实例；永不为 {@code null}
     */
    PluginRouter buildRouter();

    /**
     * 返回底层的 {@link PluginRegistry}。
     *
     * @return 插件注册表；永不为 {@code null}
     */
    PluginRegistry getRegistry();

    // ==================== 预处理器 / 后处理器管理 ====================

    /**
     * 按名称注册一个 {@link PreProcessor}。
     *
     * @param processor 要注册的预处理器；不能为 {@code null}
     */
    void registerPreProcessor(PreProcessor processor);

    /**
     * 返回以给定名称注册的预处理器。
     *
     * @param name 预处理器名称；不能为 {@code null}
     * @return 包含预处理器的 {@link Optional}，若未找到则为空
     */
    Optional<PreProcessor> getPreProcessor(String name);

    /**
     * 返回所有当前注册的预处理器。
     *
     * @return 不可修改的预处理器列表；永不为 {@code null}
     */
    List<PreProcessor> getAllPreProcessors();

    /**
     * 按名称注册一个 {@link PostProcessor}。
     *
     * @param processor 要注册的后处理器；不能为 {@code null}
     */
    void registerPostProcessor(PostProcessor processor);

    /**
     * 返回以给定名称注册的后处理器。
     *
     * @param name 后处理器名称；不能为 {@code null}
     * @return 包含后处理器的 {@link Optional}，若未找到则为空
     */
    Optional<PostProcessor> getPostProcessor(String name);

    /**
     * 返回所有当前注册的后处理器。
     *
     * @return 不可修改的后处理器列表；永不为 {@code null}
     */
    List<PostProcessor> getAllPostProcessors();
}
