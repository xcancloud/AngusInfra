package cloud.xcan.angus.plugin.manager;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.plugin.api.AbstractSampler;
import cloud.xcan.angus.plugin.api.PluginMetadata;
import cloud.xcan.angus.plugin.api.SampleContext;
import cloud.xcan.angus.plugin.api.SampleResult;
import cloud.xcan.angus.plugin.api.Sampler;
import cloud.xcan.angus.spec.model.Configuration.TestPlatform;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link AngusPluginManager#loadFromDirectory(Path)} 的端到端测试：动态生成 jar +
 * META-INF/services 条目，验证装载、卸载与 ClassLoader 关闭。
 */
class AngusPluginManagerLoadFromDirectoryTest {

    @PluginMetadata(name = "ext-http", version = "1.0.0")
    public static class ExtHttpSampler extends AbstractSampler {
        @Override
        public List<TestPlatform> getSupportedPlatforms() {
            return List.of(TestPlatform.HTTP_TESTING);
        }

        @Override
        public SampleResult sample(SampleContext ctx) {
            return createResult().success(true).endTime(System.currentTimeMillis()).build();
        }
    }

    @Test
    @DisplayName("loadFromDirectory: missing directory returns 0/0")
    void missingDirectoryNoOp(@TempDir Path tmp) {
        AngusPluginManager m = new AngusPluginManager();
        PluginLoadResult r = m.loadFromDirectory(tmp.resolve("does-not-exist"));
        assertThat(r.loaded()).isZero();
        assertThat(r.failed()).isZero();
        m.close();
    }

    @Test
    @DisplayName("loadFromDirectory: empty directory returns 0/0")
    void emptyDirectoryNoOp(@TempDir Path tmp) {
        AngusPluginManager m = new AngusPluginManager();
        PluginLoadResult r = m.loadFromDirectory(tmp);
        assertThat(r.loaded()).isZero();
        assertThat(r.failed()).isZero();
        m.close();
    }

    @Test
    @DisplayName("loadFromDirectory: registers samplers from a generated jar")
    void registersSamplerFromJar(@TempDir Path tmp) throws Exception {
        Path jar = tmp.resolve("ext-http-plugin.jar");
        writeServicesJar(jar, ExtHttpSampler.class);

        AngusPluginManager m = new AngusPluginManager();
        try {
            PluginLoadResult r = m.loadFromDirectory(tmp);

            assertThat(r.loaded()).isEqualTo(1);
            assertThat(r.failed()).isZero();
            assertThat(m.getSampler("ext-http")).isPresent();
            assertThat(m.externalClassLoaderSnapshot()).hasSize(1);
        } finally {
            m.close();
        }
    }

    @Test
    @DisplayName("unregisterSampler closes ClassLoader for last sampler in jar")
    void unregisterClosesClassLoader(@TempDir Path tmp) throws Exception {
        Path jar = tmp.resolve("ext-http-plugin.jar");
        writeServicesJar(jar, ExtHttpSampler.class);

        AngusPluginManager m = new AngusPluginManager();
        try {
            m.loadFromDirectory(tmp);

            URLClassLoader cl = m.externalClassLoaderSnapshot().keySet().iterator().next();
            m.unregisterSampler("ext-http");

            assertThat(m.getSampler("ext-http")).isEmpty();
            assertThat(m.externalClassLoaderSnapshot()).isEmpty();
            assertThat(isClosed(cl)).isTrue();
        } finally {
            m.close();
        }
    }

    @Test
    @DisplayName("loadFromDirectory: broken SPI entry is skipped, others still load")
    void brokenSpiEntrySkipped(@TempDir Path tmp) throws Exception {
        // jar1: 含有效 sampler；jar2: SPI 指向不存在的类
        Path goodJar = tmp.resolve("a-good.jar");
        writeServicesJar(goodJar, ExtHttpSampler.class);

        Path brokenJar = tmp.resolve("b-broken.jar");
        writeServicesJarRaw(brokenJar,
            "cloud.xcan.angus.plugin.manager.does.not.exist.MissingSampler\n");

        AngusPluginManager m = new AngusPluginManager();
        try {
            PluginLoadResult r = m.loadFromDirectory(tmp);

            assertThat(m.getSampler("ext-http"))
                .as("有效 jar 中的 sampler 仍应注册成功")
                .isPresent();
            assertThat(r.loaded()).isEqualTo(1);
            assertThat(r.failed()).isGreaterThanOrEqualTo(1);
            assertThat(r.failures()).anySatisfy(ex ->
                assertThat(ex).hasMessageContaining("b-broken.jar"));
        } finally {
            m.close();
        }
    }

    @Test
    @DisplayName("loadFromDirectory: companion <plugin>.libs/ directory contributes URLs")
    void companionLibsDirAddsUrls(@TempDir Path tmp) throws Exception {
        Path plugin = tmp.resolve("ext-http-plugin.jar");
        writeServicesJar(plugin, ExtHttpSampler.class);

        // 同级伴随依赖目录：放置一个无关 jar 验证它确实被纳入 ClassLoader
        Path libsDir = tmp.resolve("ext-http-plugin.libs");
        Files.createDirectories(libsDir);
        Path libJar = libsDir.resolve("dummy-lib.jar");
        try (java.io.OutputStream os = Files.newOutputStream(libJar);
                JarOutputStream jos = new JarOutputStream(os, new Manifest())) {
            JarEntry e = new JarEntry("dummy/Marker.txt");
            jos.putNextEntry(e);
            jos.write("present\n".getBytes());
            jos.closeEntry();
        }

        AngusPluginManager m = new AngusPluginManager();
        try {
            PluginLoadResult r = m.loadFromDirectory(tmp);

            assertThat(r.loaded()).isEqualTo(1);
            URLClassLoader cl = m.externalClassLoaderSnapshot().keySet().iterator().next();
            // 主 jar + 伴随 jar
            assertThat(cl.getURLs()).hasSize(2);
            assertThat(cl.getResource("dummy/Marker.txt")).isNotNull();
        } finally {
            m.close();
        }
    }

    @Test
    @DisplayName("close() unregisters samplers and closes all ClassLoaders")
    void closeReleasesAll(@TempDir Path tmp) throws Exception {
        Path jar = tmp.resolve("ext-http-plugin.jar");
        writeServicesJar(jar, ExtHttpSampler.class);

        AngusPluginManager m = new AngusPluginManager();
        m.loadFromDirectory(tmp);
        URLClassLoader cl = m.externalClassLoaderSnapshot().keySet().iterator().next();

        m.close();

        assertThat(m.getAllSamplers()).isEmpty();
        assertThat(m.externalClassLoaderSnapshot()).isEmpty();
        assertThat(isClosed(cl)).isTrue();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * 生成一个仅包含 META-INF/services/Sampler 条目的 jar，引用 host classpath 已有的
     * Sampler 实现类（PARENT_FIRST 策略下，其类型可被父 ClassLoader 解析）。
     */
    private static void writeServicesJar(Path jar, Class<? extends Sampler> samplerClass)
            throws IOException {
        writeServicesJarRaw(jar, samplerClass.getName() + "\n");
    }

    /** 任意 services 文件内容（用于构造缺失类名等损坏场景）。 */
    private static void writeServicesJarRaw(Path jar, String servicesContent) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (OutputStream os = Files.newOutputStream(jar);
                JarOutputStream jos = new JarOutputStream(os, manifest)) {
            JarEntry entry = new JarEntry(
                "META-INF/services/cloud.xcan.angus.plugin.api.Sampler");
            jos.putNextEntry(entry);
            jos.write(servicesContent.getBytes());
            jos.closeEntry();
        }
    }

    /** close() 后 URLClassPath 中所有 Loader 均被释放，findResource 对原 jar 内的条目返回 null。
     *  此行为在 JDK 8–21 均稳定，避免依赖已被移除的内部字段 {@code URLClassLoader.closed}。 */
    private static boolean isClosed(URLClassLoader cl) {
        return cl.findResource("META-INF/services/cloud.xcan.angus.plugin.api.Sampler") == null;
    }

    /** 防 IDE 警告：保证 Map 类型在测试中被静态引用。 */
    @SuppressWarnings("unused")
    private static final Map<String, ?> KEEP = Map.of();
}
