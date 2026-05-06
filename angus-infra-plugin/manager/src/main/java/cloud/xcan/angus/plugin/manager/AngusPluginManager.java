package cloud.xcan.angus.plugin.manager;

import cloud.xcan.angus.core.engine.PluginRouter;
import cloud.xcan.angus.plugin.api.PostProcessor;
import cloud.xcan.angus.plugin.api.PreProcessor;
import cloud.xcan.angus.plugin.api.Sampler;
import cloud.xcan.angus.plugin.manager.PluginsConfig.ClassLoaderStrategy;
import cloud.xcan.angus.plugin.registry.InMemoryPluginRegistry;
import cloud.xcan.angus.plugin.registry.PluginInfo;
import cloud.xcan.angus.plugin.registry.PluginRegistry;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PluginManager} 实现通过
 * {@link ServiceLoader} 发现 {@link Sampler} 插件并在 {@link ConcurrentHashMap} 中维护它们。
 *
 * <p>插件必须在
 * {@code META-INF/services/cloud.xcan.angus.plugin.api.Sampler} 中声明其 {@link Sampler} 实现
 * 以便由 {@link #loadPlugins()} 自动发现。
 *
 * <p>示例：
 * <pre>{@code
 * AngusPluginManager manager = new AngusPluginManager();
 * manager.loadPlugins();
 * PluginRouter router = manager.buildRouter();
 * HybridOrchestrator orchestrator = new HybridOrchestrator(router);
 * }</pre>
 *
 * @since 2.0.0
 */
public class AngusPluginManager implements PluginManager, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(AngusPluginManager.class);

    private final ConcurrentHashMap<String, Sampler> samplers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PreProcessor> preProcessors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PostProcessor> postProcessors = new ConcurrentHashMap<>();
    private final PluginRegistry registry = new InMemoryPluginRegistry();

    /** Sampler name → 该 Sampler 所属的 jar ClassLoader（仅外部目录加载的插件有值）。 */
    private final ConcurrentHashMap<String, URLClassLoader> samplerClassLoaders = new ConcurrentHashMap<>();
    /** ClassLoader 引用计数：同一 ClassLoader 加载多个 Sampler 时延迟关闭。 */
    private final ConcurrentHashMap<URLClassLoader, Integer> classLoaderRefCounts = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void loadPlugins() {
        loadPluginsWithResult();
    }

    /** {@inheritDoc} */
    @Override
    public PluginLoadResult loadPluginsWithResult() {
        log.info("Discovering plugins via ServiceLoader...");
        int loaded = 0;
        List<PluginLoadException> failures = new ArrayList<>();

        // 发现 Sampler 实现
        for (Sampler sampler : ServiceLoader.load(Sampler.class)) {
            try {
                registerSampler(sampler);
                loaded++;
            } catch (PluginLoadException e) {
                log.error("Failed to load plugin '{}': {}", sampler.getName(), e.getMessage(), e);
                failures.add(e);
            } catch (Exception e) {
                String pluginName = sampler.getClass().getName();
                log.error("Unexpected error loading plugin '{}': {}", pluginName, e.getMessage(), e);
                failures.add(new PluginLoadException(
                        "Unexpected error loading plugin '" + pluginName + "'", e));
            }
        }

        // 发现 PreProcessor 实现
        for (PreProcessor processor : ServiceLoader.load(PreProcessor.class)) {
            try {
                registerPreProcessor(processor);
            } catch (Exception e) {
                log.error("Failed to load pre-processor '{}': {}", processor.getName(), e.getMessage(), e);
                failures.add(new PluginLoadException(
                        "Failed to load pre-processor '" + processor.getName() + "'", e));
            }
        }

        // 发现 PostProcessor 实现
        for (PostProcessor processor : ServiceLoader.load(PostProcessor.class)) {
            try {
                registerPostProcessor(processor);
            } catch (Exception e) {
                log.error("Failed to load post-processor '{}': {}", processor.getName(), e.getMessage(), e);
                failures.add(new PluginLoadException(
                        "Failed to load post-processor '" + processor.getName() + "'", e));
            }
        }

        log.info("Loaded {} sampler(s), {} pre-processor(s), {} post-processor(s), {} failed.",
                loaded, preProcessors.size(), postProcessors.size(), failures.size());
        return new PluginLoadResult(loaded, failures.size(), failures);
    }

    /** {@inheritDoc} */
    @Override
    public PluginLoadResult loadExternalPlugins(PluginsConfig pc, String defaultDir) {
        Objects.requireNonNull(pc, "PluginsConfig must not be null");
        String dir = (pc.dir() == null || pc.dir().isBlank()) ? defaultDir : pc.dir();
        if (dir == null || dir.isBlank()) {
            log.info("External plugin directory disabled (PluginsConfig.dir is empty)");
            return new PluginLoadResult(0, 0, List.of());
        }
        Path pluginsDir = Paths.get(dir).toAbsolutePath().normalize();
        ClassLoaderStrategy strategy =
            pc.classLoaderStrategy() == ClassLoaderStrategy.CHILD_FIRST
                ? ClassLoaderStrategy.CHILD_FIRST
                : ClassLoaderStrategy.PARENT_FIRST;

        PluginLoadResult result = loadFromDirectory(pluginsDir, strategy);
        log.info("External plugin discovery (directory={}): loaded={}, failed={}, strategy={}",
            pluginsDir, result.loaded(), result.failed(), strategy);

        if (result.hasFailures() && pc.failOnError()) {
            throw new IllegalStateException(
                "External plugin load failed (" + result.failed() + " error(s)) at "
                    + pluginsDir + " and PluginsConfig.failOnError=true");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>幂等性约定：
     * <ul>
     *   <li>已存在同名 sampler，且实现类相同 → 直接跳过（不再重复 {@link Sampler#initialize()}），
     *       常见于 {@link #loadPluginsWithResult()} 被多次调用的场景，避免连接池/线程池被重复初始化。</li>
     *   <li>已存在同名 sampler，但实现类不同 → 先按 {@link #unregisterSampler(String)} 释放旧实例
     *       （含其 {@link Sampler#destroy()} 与外部 jar ClassLoader 的引用计数递减），再注册新实例，
     *       从而正确释放原 ClassLoader、避免 Metaspace / 文件句柄泄漏。</li>
     * </ul>
     */
    @Override
    public void registerSampler(Sampler sampler) {
        Objects.requireNonNull(sampler, "sampler must not be null");
        String name = sampler.getName();

        Sampler existing = samplers.get(name);
        if (existing != null) {
            if (existing == sampler || existing.getClass() == sampler.getClass()) {
                log.debug("Sampler '{}' already registered (same class {}); skip.",
                    name, existing.getClass().getName());
                return;
            }
            log.info("Replacing sampler '{}': {} -> {}",
                name, existing.getClass().getName(), sampler.getClass().getName());
            unregisterSampler(name);
        }

        try {
            sampler.initialize();
        } catch (Exception e) {
            throw new PluginLoadException(
                    "Failed to initialise sampler '" + name + "': " + e.getMessage(), e);
        }
        samplers.put(name, sampler);
        registry.register(PluginInfo.fromSampler(sampler));
        log.debug("Registered sampler '{}' version {}", name, sampler.getVersion());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Sampler> getSampler(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(samplers.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public List<Sampler> getAllSamplers() {
        return List.copyOf(samplers.values());
    }

    /** {@inheritDoc} */
    @Override
    public PluginRouter buildRouter() {
        return new PluginRouter(new ArrayList<>(samplers.values()));
    }

    /** {@inheritDoc} */
    @Override
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * 按名称注销采样器，并对其调用 {@link Sampler#destroy()}。
     *
     * <p>若未找到指定名称的采样器，则无操作。
     * <p>若采样器来自 {@link #loadFromDirectory(Path)} 装载的 jar，则递减对应 ClassLoader
     * 的引用计数，最后一个采样器注销时关闭该 ClassLoader。
     *
     * @param name 要注销的采样器名称；不能为 {@code null}
     */
    public void unregisterSampler(String name) {
        Objects.requireNonNull(name, "name must not be null");
        Sampler sampler = samplers.remove(name);
        if (sampler != null) {
            registry.unregister(name);
            try {
                sampler.destroy();
            } catch (Exception e) {
                log.warn("Error destroying sampler '{}': {}", name, e.getMessage(), e);
            }
            URLClassLoader cl = samplerClassLoaders.remove(name);
            if (cl != null) {
                classLoaderRefCounts.compute(cl, (k, v) -> {
                    int next = (v == null ? 0 : v) - 1;
                    if (next <= 0) {
                        try {
                            cl.close();
                        } catch (IOException e) {
                            log.warn("Failed to close plugin ClassLoader for '{}': {}",
                                name, e.getMessage(), e);
                        }
                        return null;
                    }
                    return next;
                });
            }
            log.debug("Unregistered sampler '{}'", name);
        }
    }

    // ── 外部插件目录加载 ──────────────────────────────────────────────────────

    /**
     * 扫描目录下所有 {@code *.jar}，使用 {@link ClassLoaderStrategy#PARENT_FIRST}
     * 装载其中通过 {@code META-INF/services/cloud.xcan.angus.plugin.api.Sampler}
     * （以及 PreProcessor / PostProcessor）声明的实现并注册。
     *
     * <p>等价于 {@code loadFromDirectory(pluginsDir, ClassLoaderStrategy.PARENT_FIRST)}。
     *
     * @param pluginsDir 插件目录；目录不存在或为空时安静返回 {@link PluginLoadResult} 全 0。
     * @return 本次目录扫描的加载结果（不影响 classpath 上由 {@link #loadPlugins()} 装载的插件）。
     */
    public PluginLoadResult loadFromDirectory(Path pluginsDir) {
        return loadFromDirectory(pluginsDir, ClassLoaderStrategy.PARENT_FIRST);
    }

    /**
     * 扫描目录下所有 {@code *.jar}，按指定 {@link ClassLoaderStrategy} 装载其中通过
     * {@code META-INF/services/...} 声明的 {@link Sampler} / {@link PreProcessor} /
     * {@link PostProcessor} 实现并注册。
     *
     * <p>同名 Sampler 会按 {@link #registerSampler(Sampler)} 的语义被覆盖；建议在卸载旧版本
     * （{@link #unregisterSampler(String)}）后再加载新 jar，以正确释放原 ClassLoader。
     *
     * <p>每个 jar 使用独立的 {@link URLClassLoader}。当 jar 中至少注册到 1 个 Sampler 时，
     * ClassLoader 的引用计数 +1；最后一个 Sampler 注销或调用 {@link #close()} 时关闭。
     *
     * @param pluginsDir 插件目录；不存在或不是目录时返回全 0 的结果，不抛异常。
     * @param strategy   类加载策略；{@code null} 等价于 {@link ClassLoaderStrategy#PARENT_FIRST}。
     * @return 本次目录扫描的加载结果。
     */
    public PluginLoadResult loadFromDirectory(Path pluginsDir, ClassLoaderStrategy strategy) {
        Objects.requireNonNull(pluginsDir, "pluginsDir must not be null");
        ClassLoaderStrategy effective = strategy != null ? strategy : ClassLoaderStrategy.PARENT_FIRST;

        if (!Files.isDirectory(pluginsDir)) {
            log.info("Plugins directory not found, skipping external load: {}", pluginsDir);
            return new PluginLoadResult(0, 0, List.of());
        }

        List<Path> jars;
        try (Stream<Path> stream = Files.list(pluginsDir)) {
            jars = stream
                .filter(p -> p.toString().endsWith(".jar") && Files.isRegularFile(p))
                .sorted()
                .toList();
        } catch (IOException e) {
            log.error("Failed to list plugin directory {}: {}", pluginsDir, e.getMessage(), e);
            return new PluginLoadResult(0, 1, List.of(
                new PluginLoadException("Failed to list plugin directory " + pluginsDir, e)));
        }
        if (jars.isEmpty()) {
            log.info("No *.jar found in plugin directory {}", pluginsDir);
            return new PluginLoadResult(0, 0, List.of());
        }

        log.info("Discovering external plugins in {} ({} jar file(s), strategy={})",
            pluginsDir, jars.size(), effective);

        int loaded = 0;
        List<PluginLoadException> failures = new ArrayList<>();
        ClassLoader parent = chooseParentClassLoader();

        for (Path jar : jars) {
            try {
                int registered = loadFromJar(jar, parent, effective, failures);
                loaded += registered;
            } catch (Exception e) {
                log.error("Failed to load plugin jar '{}': {}", jar.getFileName(), e.getMessage(), e);
                failures.add(new PluginLoadException(
                    "Failed to load plugin jar '" + jar.getFileName() + "'", e));
            }
        }

        log.info("External plugin discovery complete: dir={}, loaded={}, failed={}",
            pluginsDir, loaded, failures.size());
        return new PluginLoadResult(loaded, failures.size(), failures);
    }

    /**
     * 加载单个 jar；返回成功注册到本管理器的 {@link Sampler} 数量
     * （PreProcessor / PostProcessor 数量记入日志，但不计入返回值，以与 loadPluginsWithResult 语义一致）。
     *
     * <p>使用显式 {@link Iterator#hasNext()}/{@link Iterator#next()} 调用，确保单个 SPI 条目
     * 因 {@link ServiceConfigurationError}（如缺失依赖、缺无参构造）失败时仅跳过该条目而非中止整个 jar。
     */
    private int loadFromJar(Path jar, ClassLoader parent, ClassLoaderStrategy strategy,
        List<PluginLoadException> failures) throws IOException {
        URL[] urls = buildPluginUrls(jar);
        if (log.isDebugEnabled() && urls.length > 1) {
            log.debug("Plugin {} ClassLoader URLs: {}", jar.getFileName(), urls.length);
        }
        URLClassLoader classLoader = strategy == ClassLoaderStrategy.CHILD_FIRST
            ? new ChildFirstClassLoader(urls, parent)
            : new URLClassLoader(urls, parent);

        // 用单元素数组以避免 lambda 中 effectively-final 的限制
        final boolean[] retainClassLoader = {false};
        final int[] registered = {0};

        try {
            iterateServices(jar, ServiceLoader.load(Sampler.class, classLoader),
                Sampler.class, failures, sampler -> {
                    String name = safeName(sampler);
                    try {
                        registerSampler(sampler);
                        samplerClassLoaders.put(name, classLoader);
                        classLoaderRefCounts.merge(classLoader, 1, Integer::sum);
                        registered[0]++;
                        retainClassLoader[0] = true;
                    } catch (PluginLoadException e) {
                        log.error("Failed to load plugin '{}' from {}: {}",
                            name, jar.getFileName(), e.getMessage(), e);
                        failures.add(e);
                    } catch (Exception e) {
                        log.error("Unexpected error loading plugin '{}' from {}: {}",
                            name, jar.getFileName(), e.getMessage(), e);
                        failures.add(new PluginLoadException(
                            "Unexpected error loading plugin '" + name + "' from " + jar.getFileName(), e));
                    }
                });

            iterateServices(jar, ServiceLoader.load(PreProcessor.class, classLoader),
                PreProcessor.class, failures, proc -> {
                    try {
                        registerPreProcessor(proc);
                        retainClassLoader[0] = true;
                    } catch (Exception e) {
                        log.error("Failed to load pre-processor '{}' from {}: {}",
                            proc.getName(), jar.getFileName(), e.getMessage(), e);
                        failures.add(new PluginLoadException(
                            "Failed to load pre-processor '" + proc.getName() + "' from " + jar.getFileName(), e));
                    }
                });

            iterateServices(jar, ServiceLoader.load(PostProcessor.class, classLoader),
                PostProcessor.class, failures, proc -> {
                    try {
                        registerPostProcessor(proc);
                        retainClassLoader[0] = true;
                    } catch (Exception e) {
                        log.error("Failed to load post-processor '{}' from {}: {}",
                            proc.getName(), jar.getFileName(), e.getMessage(), e);
                        failures.add(new PluginLoadException(
                            "Failed to load post-processor '" + proc.getName() + "' from " + jar.getFileName(), e));
                    }
                });
        } finally {
            if (!retainClassLoader[0]) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    log.warn("Failed to close ClassLoader of empty plugin jar {}: {}",
                        jar.getFileName(), e.getMessage(), e);
                }
                if (registered[0] == 0) {
                    log.warn("Plugin jar contained no Sampler/PreProcessor/PostProcessor implementations: {}",
                        jar.getFileName());
                }
            }
        }
        return registered[0];
    }

    /**
     * 显式驱动 {@link ServiceLoader} 迭代器，将单个条目的 {@link ServiceConfigurationError}
     * 转换为 {@link PluginLoadException} 并记入 {@code failures}，继续遍历剩余条目。
     *
     * <p>典型 {@code ServiceConfigurationError} 来源：
     * <ul>
     *   <li>{@link NoClassDefFoundError}（缺失运行时依赖）</li>
     *   <li>SPI 类没有公共无参构造</li>
     *   <li>SPI 类不是接口的实现</li>
     * </ul>
     */
    private static <S> void iterateServices(Path jar, ServiceLoader<S> loader,
        Class<S> spi, List<PluginLoadException> failures, Consumer<S> onEach) {
        Iterator<S> it = loader.iterator();
        while (true) {
            S svc;
            try {
                if (!it.hasNext()) {
                    return;
                }
                svc = it.next();
            } catch (ServiceConfigurationError e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log.error("Skipping broken {} entry in {}: {}",
                    spi.getSimpleName(), jar.getFileName(), cause.toString());
                failures.add(new PluginLoadException(
                    "Broken " + spi.getSimpleName() + " entry in " + jar.getFileName()
                        + ": " + cause.getMessage(), cause));
                continue; // 跳过该条目，继续下一个
            }
            try {
                onEach.accept(svc);
            } catch (RuntimeException e) {
                log.error("Unexpected error processing {} '{}' in {}: {}",
                    spi.getSimpleName(), svc.getClass().getName(), jar.getFileName(),
                    e.getMessage(), e);
                failures.add(new PluginLoadException(
                    "Unexpected error processing " + spi.getSimpleName() + " '"
                        + svc.getClass().getName() + "' in " + jar.getFileName(), e));
            }
        }
    }

    private static String safeName(Sampler sampler) {
        try {
            String n = sampler.getName();
            return n != null ? n : sampler.getClass().getName();
        } catch (Exception ignore) {
            return sampler.getClass().getName();
        }
    }

    /**
     * 为插件 jar 构造完整的 ClassLoader URL 列表：主 jar + 同级伴随依赖目录中的所有 jar。
     *
     * <p>约定：插件 {@code foo.jar} 的私有依赖位于以下任一同级目录（按优先级查找，命中即停）：
     * <ol>
     *   <li>{@code foo.libs/}（推荐）</li>
     *   <li>{@code foo-libs/}</li>
     *   <li>{@code foo/}（与主 jar 同名的目录）</li>
     * </ol>
     *
     * <p>这样每个插件的第三方库（如 grpc-netty / kafka-clients / jedis 等）随插件 jar 一并发布，
     * 由独立的 {@link URLClassLoader} 加载，相互隔离也与父 ClassLoader 隔离，避免库版本冲突。
     */
    private static URL[] buildPluginUrls(Path jar) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(jar.toUri().toURL());

        Path companionDir = resolveCompanionLibsDir(jar);
        if (companionDir != null) {
            try (Stream<Path> stream = Files.list(companionDir)) {
                List<Path> libs = stream
                    .filter(p -> p.toString().endsWith(".jar") && Files.isRegularFile(p))
                    .sorted()
                    .toList();
                for (Path lib : libs) {
                    urls.add(lib.toUri().toURL());
                }
                if (!libs.isEmpty()) {
                    log.debug("Plugin {} loaded {} companion lib(s) from {}",
                        jar.getFileName(), libs.size(), companionDir.getFileName());
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    /** 选择主 jar 的伴随依赖目录；不存在时返回 null。 */
    private static Path resolveCompanionLibsDir(Path jar) {
        String fileName = jar.getFileName().toString();
        String stem = fileName.endsWith(".jar")
            ? fileName.substring(0, fileName.length() - 4)
            : fileName;
        Path parent = jar.getParent();
        if (parent == null) {
            return null;
        }
        for (String suffix : new String[]{".libs", "-libs", ""}) {
            Path candidate = parent.resolve(stem + suffix);
            if (suffix.isEmpty() && candidate.equals(jar)) {
                continue; // 不能把主 jar 当目录
            }
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 选取插件 jar 的父 ClassLoader：优先使用线程上下文 ClassLoader，回退到加载本类的 ClassLoader。
     * 这样可以保证 plugin API（{@link Sampler} 等）由父 ClassLoader 解析，避免 child-first 模式下的
     * 类型不一致。
     */
    private static ClassLoader chooseParentClassLoader() {
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        return ctx != null ? ctx : AngusPluginManager.class.getClassLoader();
    }

    /** 当前由本管理器持有的外部插件 ClassLoader 与其 sampler 名集合（用于自省 / 卸载）。 */
    public Map<URLClassLoader, List<String>> externalClassLoaderSnapshot() {
        Map<URLClassLoader, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, URLClassLoader> e : samplerClassLoaders.entrySet()) {
            out.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * 关闭本管理器：注销所有采样器并关闭由 {@link #loadFromDirectory(Path)} 装载的 ClassLoader。
     */
    @Override
    public void close() {
        for (String name : List.copyOf(samplers.keySet())) {
            unregisterSampler(name);
        }
        // 兜底：少量 ClassLoader 可能未通过 unregisterSampler 关闭
        for (URLClassLoader cl : List.copyOf(classLoaderRefCounts.keySet())) {
            try {
                cl.close();
            } catch (IOException e) {
                log.warn("Failed to close residual plugin ClassLoader: {}", e.getMessage(), e);
            }
        }
        classLoaderRefCounts.clear();
        samplerClassLoaders.clear();
    }

    /**
     * Child-first {@link URLClassLoader}：本地 jar 优先；plugin API 包仍委派父 ClassLoader，
     * 防止 {@link Sampler}/{@link PreProcessor}/{@link PostProcessor} 出现 {@code ClassCastException}。
     */
    private static final class ChildFirstClassLoader extends URLClassLoader {

        private static final String[] PARENT_PREFIXES = {
            "java.", "javax.", "jakarta.", "sun.", "jdk.",
            "cloud.xcan.angus.plugin.api.",
            "cloud.xcan.angus.plugin.registry.",
            "cloud.xcan.angus.core.engine.",
            "org.slf4j.", "org.apache.logging.",
        };

        ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null && delegateToParent(name)) {
                    try {
                        c = getParent().loadClass(name);
                    } catch (ClassNotFoundException ignored) {
                        // fall back to local
                    }
                }
                if (c == null) {
                    try {
                        c = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        c = super.loadClass(name, false);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }

        private static boolean delegateToParent(String className) {
            for (String prefix : PARENT_PREFIXES) {
                if (className.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ── 预处理器 / 后处理器管理 ────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>同名+同类直接跳过；同名+异类替换并记录 INFO 日志（与 sampler 幂等语义一致）。
     */
    @Override
    public void registerPreProcessor(PreProcessor processor) {
        Objects.requireNonNull(processor, "processor must not be null");
        String name = processor.getName();
        PreProcessor existing = preProcessors.get(name);
        if (existing != null) {
            if (existing == processor || existing.getClass() == processor.getClass()) {
                return;
            }
            log.info("Replacing pre-processor '{}': {} -> {}",
                name, existing.getClass().getName(), processor.getClass().getName());
        }
        preProcessors.put(name, processor);
        log.debug("Registered pre-processor '{}'", name);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PreProcessor> getPreProcessor(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(preProcessors.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public List<PreProcessor> getAllPreProcessors() {
        return List.copyOf(preProcessors.values());
    }

    /**
     * {@inheritDoc}
     *
     * <p>同名+同类直接跳过；同名+异类替换并记录 INFO 日志（与 sampler 幂等语义一致）。
     */
    @Override
    public void registerPostProcessor(PostProcessor processor) {
        Objects.requireNonNull(processor, "processor must not be null");
        String name = processor.getName();
        PostProcessor existing = postProcessors.get(name);
        if (existing != null) {
            if (existing == processor || existing.getClass() == processor.getClass()) {
                return;
            }
            log.info("Replacing post-processor '{}': {} -> {}",
                name, existing.getClass().getName(), processor.getClass().getName());
        }
        postProcessors.put(name, processor);
        log.debug("Registered post-processor '{}'", name);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PostProcessor> getPostProcessor(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(postProcessors.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public List<PostProcessor> getAllPostProcessors() {
        return List.copyOf(postProcessors.values());
    }
}
