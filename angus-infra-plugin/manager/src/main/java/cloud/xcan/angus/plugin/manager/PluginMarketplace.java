package cloud.xcan.angus.plugin.manager;

import cloud.xcan.angus.plugin.registry.MarketplaceEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理远程插件市场：目录发现、带完整性验证的下载以及动态安装。
 *
 * <p>市场目录是从可配置 HTTP 端点提供的 {@link MarketplaceEntry} 对象的 JSON 数组。
 * 插件以 JAR 形式下载到本地目录，并通过子 {@link URLClassLoader} +
 * {@link ServiceLoader} 加载。
 *
 * <p>典型流程：
 * <ol>
 *   <li>{@link #fetchCatalog()} — 获取所有可用插件</li>
 *   <li>用户选择一个条目 → {@link #install(MarketplaceEntry, AngusPluginManager)}</li>
 *   <li>下载 JAR、验证 SHA-256、加载并注册</li>
 * </ol>
 *
 * <p>此类实现了 {@link AutoCloseable}。调用 {@link #close()} 后，
 * 所有方法都将抛出 {@link IllegalStateException}。
 *
 * @since 2.0.0
 */
public class PluginMarketplace implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PluginMarketplace.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /** 最大下载重试次数。 */
    private static final int MAX_RETRIES = 3;
    /** 重试之间的初始延迟（每次尝试翻倍）。 */
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(1);
    /** 默认目录缓存 TTL。 */
    private static final Duration CATALOG_CACHE_TTL = Duration.ofMinutes(5);

    private final String catalogUrl;
    private final Path pluginsDir;
    private final HttpClient httpClient;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    /** 按插件 id 跟踪类加载器，以确保采样器类使用期间类加载器保持打开。 */
    private final ConcurrentHashMap<String, URLClassLoader> pluginClassLoaders = new ConcurrentHashMap<>();

    /** 已缓存的目录条目。 */
    private volatile List<MarketplaceEntry> cachedCatalog;
    /** 最后一次成功获取目录的时间戳。 */
    private volatile long catalogFetchedAt;

    /**
     * @param catalogUrl JSON 插件目录的 URL
     * @param pluginsDir 存放已下载插件 JAR 的本地目录
     */
    public PluginMarketplace(String catalogUrl, Path pluginsDir) {
        this.catalogUrl = Objects.requireNonNull(catalogUrl, "catalogUrl");
        this.pluginsDir = Objects.requireNonNull(pluginsDir, "pluginsDir");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 从远端获取可用插件目录。
     *
     * @return 市场条目列表
     * @throws IOException 若 HTTP 请求或 JSON 解析失败
     */
    public List<MarketplaceEntry> fetchCatalog() throws IOException, InterruptedException {
        ensureOpen();
        // 若缓存仍在 TTL 有效期内则直接返回
        List<MarketplaceEntry> cached = cachedCatalog;
        if (cached != null && (System.currentTimeMillis() - catalogFetchedAt) < CATALOG_CACHE_TTL.toMillis()) {
            return cached;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(catalogUrl))
                .GET()
                .timeout(TIMEOUT)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Catalog fetch failed with HTTP " + response.statusCode());
        }
        List<MarketplaceEntry> catalog = MAPPER.readValue(response.body(), new TypeReference<>() {});
        cachedCatalog = catalog;
        catalogFetchedAt = System.currentTimeMillis();
        return catalog;
    }

    /**
     * 从市场下载并安装插件。
     *
     * <p>步骤：
     * <ol>
     *   <li>将 JAR 下载到 {@code pluginsDir/}</li>
     *   <li>验证 SHA-256 校验和（若已提供）</li>
     *   <li>加载 JAR 并注册发现的 {@code Sampler} 实现</li>
     * </ol>
     *
     * @param entry   描述插件的市场条目
     * @param manager 发现的采样器将注册到的插件管理器
     * @return 已安装 JAR 的路径
     * @throws IOException          若下载或验证失败
     * @throws InterruptedException 若下载期间被中断
     */
    public Path install(MarketplaceEntry entry, AngusPluginManager manager)
            throws IOException, InterruptedException {
        ensureOpen();
        Files.createDirectories(pluginsDir);

        String filename = entry.id() + "-" + entry.version() + ".jar";
        Path target = pluginsDir.resolve(filename);

        log.info("Downloading plugin '{}' v{} from {}", entry.name(), entry.version(), entry.downloadUrl());

        // 带指数退避重试地下载
        downloadWithRetry(entry.downloadUrl(), target);

        // 验证校验和
        if (entry.checksumSha256() != null && !entry.checksumSha256().isBlank()) {
            String actual = sha256(target);
            if (!actual.equalsIgnoreCase(entry.checksumSha256())) {
                Files.deleteIfExists(target);
                throw new IOException("SHA-256 mismatch for plugin '" + entry.name()
                        + "': expected " + entry.checksumSha256() + " but got " + actual);
            }
            log.debug("SHA-256 verified for '{}'", filename);
        }

        // 将 JAR 加载到子类加载器并发现 Sampler 实现。
        // 只要采样器类还在使用，类加载器必须保持打开状态。
        var jarUrl = target.toUri().toURL();
        var classLoader = new URLClassLoader(
                new java.net.URL[]{jarUrl}, Thread.currentThread().getContextClassLoader());
        try {
            var loader = ServiceLoader.load(cloud.xcan.angus.plugin.api.Sampler.class, classLoader);
            int count = 0;
            for (var sampler : loader) {
                manager.registerSampler(sampler);
                count++;
            }
            if (count == 0) {
                classLoader.close();
                log.warn("Plugin '{}' v{} contained no Sampler implementations", entry.name(), entry.version());
            } else {
                pluginClassLoaders.put(entry.id(), classLoader);
                log.info("Installed plugin '{}' v{} — {} sampler(s) registered",
                        entry.name(), entry.version(), count);
            }
        } catch (Exception e) {
            classLoader.close();
            throw e;
        }

        return target;
    }

    /**
     * 使用指数退避重试从给定 URL 下载文件。
     *
     * <p>在瞬时失败（HTTP 5xx、连接错误）时最多重试 {@link #MAX_RETRIES} 次。
     * 每次重试之间的延迟从 {@link #INITIAL_RETRY_DELAY} 开始，每次翻倍。
     *
     * @param url    要下载的 URL
     * @param target 要保存到的本地文件路径
     * @throws IOException          若所有重试均失败
     * @throws InterruptedException 若在下载或重试延迟期间被中断
     */
    private void downloadWithRetry(String url, Path target) throws IOException, InterruptedException {
        IOException lastException = null;
        Duration delay = INITIAL_RETRY_DELAY;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofMinutes(5))
                        .build();
                HttpResponse<InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    try (InputStream in = response.body()) {
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return; // 成功
                } else if (statusCode >= 500 && statusCode < 600 && attempt < MAX_RETRIES) {
                    // 服务端错误——退避后重试
                    log.warn("Download attempt {}/{} failed with HTTP {} - retrying in {}",
                            attempt, MAX_RETRIES, statusCode, delay);
                    lastException = new IOException("HTTP " + statusCode);
                } else {
                    // 客户端错误或最后一次尝试——立即失败
                    throw new IOException("Plugin download failed with HTTP " + statusCode);
                }
            } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
                // 网络错误——退避后重试
                if (attempt < MAX_RETRIES) {
                    log.warn("Download attempt {}/{} failed with {} - retrying in {}",
                            attempt, MAX_RETRIES, e.getClass().getSimpleName(), delay);
                    lastException = new IOException("Network error: " + e.getMessage(), e);
                } else {
                    throw new IOException("Network error after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
            }

            // 指数退避等待后进行下次重试
            Thread.sleep(delay.toMillis());
            delay = delay.multipliedBy(2);
        }

        // 理论上不会执行到此处，但作为边界情况处理
        throw lastException != null ? lastException : new IOException("Download failed after " + MAX_RETRIES + " attempts");
    }

    /**
     * 通过删除其 JAR 并注销其采样器来卸载插件。
     *
     * @param pluginId 插件标识符
     * @param manager  插件管理器
     * @return 若插件已被卸载则返回 {@code true}
     */
    public boolean uninstall(String pluginId, AngusPluginManager manager) throws IOException {
        ensureOpen();
        manager.unregisterSampler(pluginId);
        // 关闭此插件的类加载器
        URLClassLoader cl = pluginClassLoaders.remove(pluginId);
        if (cl != null) {
            cl.close();
        }
        // 尝试删除与插件 id 匹配的 JAR 文件
        boolean deleted = false;
        try (var stream = Files.list(pluginsDir)) {
            var jars = stream.filter(p -> p.getFileName().toString().startsWith(pluginId + "-")).toList();
            for (Path jar : jars) {
                Files.deleteIfExists(jar);
                deleted = true;
            }
        }
        if (deleted) {
            log.info("Uninstalled plugin '{}'", pluginId);
        }
        return deleted;
    }

    /**
     * 列出当前已安装在本地目录中的插件。
     *
     * @return JAR 文件名列表
     */
    public List<String> listInstalled() throws IOException {
        ensureOpen();
        if (!Files.isDirectory(pluginsDir)) {
            return List.of();
        }
        try (var stream = Files.list(pluginsDir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(file));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
    }

    /**
     * 关闭此市场实例。
     *
     * <p>调用此方法后，所有其他方法都将抛出 {@link IllegalStateException}。
     * 底层 {@link HttpClient} 资源将在 JVM 终止时释放。
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            // 关闭所有插件的类加载器
            for (var entry : pluginClassLoaders.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (IOException e) {
                    log.warn("Failed to close classloader for plugin '{}'", entry.getKey(), e);
                }
            }
            pluginClassLoaders.clear();
            log.debug("PluginMarketplace closed");
        }
    }

    /**
     * 返回此市场是否已被关闭。
     *
     * @return 若 {@link #close()} 已被调用则返回 {@code true}
     */
    public boolean isClosed() {
        return closed.get();
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("PluginMarketplace has been closed");
        }
    }
}
