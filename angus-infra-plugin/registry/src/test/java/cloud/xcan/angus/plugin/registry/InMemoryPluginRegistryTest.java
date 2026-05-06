package cloud.xcan.angus.plugin.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link InMemoryPluginRegistry} 的单元测试。
 *
 * @since 2.0.0
 */
class InMemoryPluginRegistryTest {

    private InMemoryPluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryPluginRegistry();
    }

    @Test
    @DisplayName("register and findById returns the registered plugin")
    void registerAndFindById() {
        PluginInfo info = PluginInfo.of("angus-http", "angus-http", "2.0.0",
                "HTTP sampler", List.of("HTTP_TESTING", "HYBRID"), List.of("http"));
        registry.register(info);

        Optional<PluginInfo> found = registry.findById("angus-http");
        assertThat(found).isPresent().contains(info);
    }

    @Test
    @DisplayName("findByName returns plugin with matching name")
    void findByName() {
        PluginInfo info = PluginInfo.of("angus-http", "angus-http", "2.0.0",
                "", List.of("HTTP_TESTING"), List.of());
        registry.register(info);

        assertThat(registry.findByName("angus-http")).isPresent();
        assertThat(registry.findByName("unknown")).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all registered plugins")
    void findAll() {
        registry.register(PluginInfo.of("a", "a", "1.0.0", "", List.of(), List.of()));
        registry.register(PluginInfo.of("b", "b", "1.0.0", "", List.of(), List.of()));

        assertThat(registry.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("findByPlatform filters by platform name")
    void findByPlatform() {
        registry.register(PluginInfo.of("http", "http", "1.0", "",
                List.of("HTTP_TESTING", "HYBRID"), List.of()));
        registry.register(PluginInfo.of("ws", "ws", "1.0", "",
                List.of("WEBSOCKET"), List.of()));

        List<PluginInfo> httpPlugins = registry.findByPlatform("HTTP_TESTING");
        assertThat(httpPlugins).hasSize(1);
        assertThat(httpPlugins.get(0).id()).isEqualTo("http");
    }

    @Test
    @DisplayName("unregister removes the plugin")
    void unregister() {
        registry.register(PluginInfo.of("angus-http", "angus-http", "2.0.0",
                "", List.of(), List.of()));
        assertThat(registry.size()).isEqualTo(1);

        registry.unregister("angus-http");
        assertThat(registry.size()).isZero();
        assertThat(registry.findById("angus-http")).isEmpty();
    }

    @Test
    @DisplayName("register with same id overwrites previous entry")
    void registerOverwrite() {
        registry.register(PluginInfo.of("id", "v1", "1.0.0", "", List.of(), List.of()));
        registry.register(PluginInfo.of("id", "v2", "2.0.0", "", List.of(), List.of()));

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.findById("id").map(PluginInfo::name)).contains("v2");
    }

    @Test
    @DisplayName("PluginInfo configSchema defaults to empty when using 6-arg factory")
    void pluginInfoConfigSchema_defaultsToEmpty() {
        PluginInfo info = PluginInfo.of("id", "name", "1.0", "", List.of(), List.of());
        assertThat(info.configSchema()).isEmpty();
    }

    @Test
    @DisplayName("PluginInfo configSchema is preserved via 7-arg factory")
    void pluginInfoConfigSchema_preservedVia7ArgFactory() {
        String schema = "{\"type\":\"object\"}";
        PluginInfo info = PluginInfo.of("id", "name", "1.0", "", List.of(), List.of(), schema);
        assertThat(info.configSchema()).isEqualTo(schema);
    }

    @Test
    @DisplayName("PluginInfo null configSchema is normalised to empty string")
    void pluginInfoConfigSchema_nullNormalisedToEmpty() {
        PluginInfo info = PluginInfo.of("id", "name", "1.0", "", List.of(), List.of(), null);
        assertThat(info.configSchema()).isEmpty();
    }

    @Test
    @DisplayName("register with null throws NullPointerException")
    void registerNullThrows() {
        assertThatThrownBy(() -> registry.register(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("size returns correct count")
    void size() {
        assertThat(registry.size()).isZero();
        registry.register(PluginInfo.of("a", "a", "1.0", "", List.of(), List.of()));
        assertThat(registry.size()).isEqualTo(1);
    }
}
