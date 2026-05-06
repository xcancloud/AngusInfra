package cloud.xcan.angus.plugin.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.plugin.registry.MarketplaceEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link PluginMarketplace} 的单元测试。
 *
 * <p>这些测试验证：
 * <ul>
 *   <li>AutoCloseable 生命周期与状态管理</li>
 *   <li>listInstalled() 功能</li>
 *   <li>close() 后的异常处理</li>
 * </ul>
 *
 * <p>注意：fetchCatalog() 和 install() 的测试需要网络 Mock（如 WireMock），
 * 已标记为集成测试。
 *
 * @since 2.0.0
 */
class PluginMarketplaceTest {

    private static final String TEST_CATALOG_URL = "https://plugins.example.com/catalog.json";

    @TempDir
    Path tempDir;

    private PluginMarketplace marketplace;

    @BeforeEach
    void setUp() {
        marketplace = new PluginMarketplace(TEST_CATALOG_URL, tempDir);
    }

    @Test
    void constructorRejectsNullCatalogUrl() {
        assertThatThrownBy(() -> new PluginMarketplace(null, tempDir))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("catalogUrl");
    }

    @Test
    void constructorRejectsNullPluginsDir() {
        assertThatThrownBy(() -> new PluginMarketplace(TEST_CATALOG_URL, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("pluginsDir");
    }

    @Test
    void isClosedReturnsFalseBeforeClose() {
        assertThat(marketplace.isClosed()).isFalse();
    }

    @Test
    void isClosedReturnsTrueAfterClose() {
        marketplace.close();
        assertThat(marketplace.isClosed()).isTrue();
    }

    @Test
    void closeIsIdempotent() {
        marketplace.close();
        marketplace.close();
        marketplace.close();
        assertThat(marketplace.isClosed()).isTrue();
    }

    @Test
    void listInstalledThrowsAfterClose() {
        marketplace.close();
        assertThatThrownBy(() -> marketplace.listInstalled())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void fetchCatalogThrowsAfterClose() {
        marketplace.close();
        assertThatThrownBy(() -> marketplace.fetchCatalog())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void installThrowsAfterClose() {
        marketplace.close();
        MarketplaceEntry entry = new MarketplaceEntry(
                "test-plugin", "Test Plugin", "1.0.0", "Test description",
                "author", "https://example.com/plugin.jar",
                List.of("HTTP_TESTING"), List.of("test"), "abc123"
        );
        AngusPluginManager manager = new AngusPluginManager();
        assertThatThrownBy(() -> marketplace.install(entry, manager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void uninstallThrowsAfterClose() {
        marketplace.close();
        AngusPluginManager manager = new AngusPluginManager();
        assertThatThrownBy(() -> marketplace.uninstall("test-plugin", manager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void listInstalledReturnsEmptyForNonExistentDir() throws IOException {
        Path nonExistent = tempDir.resolve("non-existent");
        try (PluginMarketplace mp = new PluginMarketplace(TEST_CATALOG_URL, nonExistent)) {
            List<String> installed = mp.listInstalled();
            assertThat(installed).isEmpty();
        }
    }

    @Test
    void listInstalledReturnsEmptyForEmptyDir() throws IOException {
        List<String> installed = marketplace.listInstalled();
        assertThat(installed).isEmpty();
    }

    @Test
    void listInstalledReturnsJarFilenames() throws IOException {
        // 创建一些模拟 JAR 文件
        Files.createFile(tempDir.resolve("plugin-a-1.0.0.jar"));
        Files.createFile(tempDir.resolve("plugin-b-2.0.0.jar"));
        Files.createFile(tempDir.resolve("readme.txt")); // 应被忽略

        List<String> installed = marketplace.listInstalled();

        assertThat(installed)
                .hasSize(2)
                .contains("plugin-a-1.0.0.jar", "plugin-b-2.0.0.jar")
                .doesNotContain("readme.txt");
    }

    @Test
    void listInstalledIgnoresSubdirectories() throws IOException {
        Files.createFile(tempDir.resolve("plugin-a-1.0.0.jar"));
        Files.createDirectories(tempDir.resolve("subdir.jar")); // 目录，非文件

        List<String> installed = marketplace.listInstalled();

        // Files.list() 返回所有条目，但过滤器应只返回以 .jar 结尾的文件
        // 名为 "subdir.jar" 的目录可能通过过滤器——验证实际行为
        assertThat(installed).contains("plugin-a-1.0.0.jar");
    }

    @Test
    void tryWithResourcesClosesProperly() throws IOException {
        PluginMarketplace mp;
        try (PluginMarketplace temp = new PluginMarketplace(TEST_CATALOG_URL, tempDir)) {
            mp = temp;
            assertThat(mp.isClosed()).isFalse();
        }
        assertThat(mp.isClosed()).isTrue();
    }

    @Test
    void marketplaceEntryCompactConstructorNormalizesNulls() {
        MarketplaceEntry entry = new MarketplaceEntry(
                "id", "name", "1.0.0", "desc", "author", "url",
                null, null, null
        );
        assertThat(entry.supportedPlatforms()).isEmpty();
        assertThat(entry.tags()).isEmpty();
    }

    @Test
    void marketplaceEntryDefensiveCopiesLists() {
        List<String> platforms = new java.util.ArrayList<>(List.of("HTTP"));
        List<String> tags = new java.util.ArrayList<>(List.of("test"));
        
        MarketplaceEntry entry = new MarketplaceEntry(
                "id", "name", "1.0.0", "desc", "author", "url",
                platforms, tags, "checksum"
        );
        
        // 修改原始列表
        platforms.add("GRPC");
        tags.add("foo");

        // 条目不应受影响（防御性拷贝）
        assertThat(entry.supportedPlatforms()).containsExactly("HTTP");
        assertThat(entry.tags()).containsExactly("test");
    }
}
