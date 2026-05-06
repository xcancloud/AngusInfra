package cloud.xcan.angus.plugin.manager;

import cloud.xcan.angus.plugin.api.PostProcessor;
import cloud.xcan.angus.plugin.api.PreProcessor;
import cloud.xcan.angus.plugin.api.Sampler;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * 外部插件目录配置。
 *
 * @param dir                 插件目录；相对路径基于工作目录解析；空字符串表示禁用目录加载。
 * @param classLoaderStrategy 类加载策略，默认 {@link ClassLoaderStrategy#PARENT_FIRST}。
 * @param failOnError         true 时任一 jar 加载失败即中止启动；false 仅记录日志。
 */
public record PluginsConfig(
    String dir,
    ClassLoaderStrategy classLoaderStrategy,
    boolean failOnError
) {

    public PluginsConfig {
        dir = dir != null ? dir.trim() : "";
        classLoaderStrategy = classLoaderStrategy != null
            ? classLoaderStrategy : ClassLoaderStrategy.PARENT_FIRST;
    }

    public static PluginsConfig defaults() {
        return new PluginsConfig("plugins", ClassLoaderStrategy.PARENT_FIRST, false);
    }

    /**
     * 类加载策略 —— 仅作用于 {@link AngusPluginManager#loadFromDirectory(Path, ClassLoaderStrategy)}。
     *
     * <p>不影响通过 ServiceLoader 自动发现的 classpath 上的插件。
     */
    public enum ClassLoaderStrategy {
        /**
         * <b>父优先（默认）</b>：标准 {@link URLClassLoader} 行为；jar 内类先委派给父 ClassLoader，
         * 适合插件不与宿主进程冲突、共享通用依赖的场景。
         */
        PARENT_FIRST,
        /**
         * <b>子优先（child-first）</b>：jar 内类优先在 jar 中加载，找不到再委派父；
         * 用于插件携带与宿主进程不兼容的依赖版本时做隔离。
         * <p>仍保留 {@link Sampler}/{@link PreProcessor}/{@link PostProcessor} 等 plugin API
         * 由父 ClassLoader 加载，避免 {@code ClassCastException}。
         */
        CHILD_FIRST
    }

}
