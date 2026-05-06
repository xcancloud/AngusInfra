package cloud.xcan.angus.plugin.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.core.engine.PluginRouter;
import cloud.xcan.angus.plugin.api.AbstractSampler;
import cloud.xcan.angus.plugin.api.PluginMetadata;
import cloud.xcan.angus.plugin.api.SampleContext;
import cloud.xcan.angus.plugin.api.SampleResult;
import cloud.xcan.angus.plugin.registry.PluginInfo;
import cloud.xcan.angus.spec.model.Configuration.TestPlatform;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link AngusPluginManager} 的单元测试。
 *
 * @since 2.0.0
 */
class AngusPluginManagerTest {

    // ── 测试用采样器 ──────────────────────────────────────────────────────────

    @PluginMetadata(name = "test-http", version = "1.0.0")
    static class TestHttpSampler extends AbstractSampler {
        boolean initialized = false;
        boolean destroyed = false;
        int initCount = 0;
        int destroyCount = 0;

        @Override
        public void initialize() { initialized = true; initCount++; }

        @Override
        public void destroy() { destroyed = true; destroyCount++; }

        @Override
        public List<TestPlatform> getSupportedPlatforms() {
            return List.of(TestPlatform.HTTP_TESTING, TestPlatform.HYBRID);
        }

        @Override
        public SampleResult sample(SampleContext ctx) {
            return createResult().success(true).endTime(System.currentTimeMillis()).build();
        }
    }

    @PluginMetadata(name = "test-web", version = "2.0.0")
    static class TestWebSampler extends AbstractSampler {
        @Override
        public List<TestPlatform> getSupportedPlatforms() {
            return List.of(TestPlatform.WEB_TESTING);
        }

        @Override
        public SampleResult sample(SampleContext ctx) {
            return createResult().success(true).endTime(System.currentTimeMillis()).build();
        }
    }

    // ── 初始化 ────────────────────────────────────────────────────────────────

    private AngusPluginManager manager;

    @BeforeEach
    void setUp() {
        manager = new AngusPluginManager();
    }

    // ── 测试 ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerSampler stores sampler and calls initialize()")
    void registerSamplerCallsInitialize() {
        TestHttpSampler sampler = new TestHttpSampler();
        manager.registerSampler(sampler);

        assertThat(sampler.initialized).isTrue();
        assertThat(manager.getSampler("test-http")).isPresent().contains(sampler);
    }

    @Test
    @DisplayName("getSampler returns empty for unknown name")
    void getSamplerUnknownName() {
        assertThat(manager.getSampler("not-registered")).isEmpty();
    }

    @Test
    @DisplayName("getAllSamplers returns all registered samplers")
    void getAllSamplers() {
        manager.registerSampler(new TestHttpSampler());
        manager.registerSampler(new TestWebSampler());

        assertThat(manager.getAllSamplers()).hasSize(2);
    }

    @Test
    @DisplayName("buildRouter routes steps to the correct sampler")
    void buildRouter() {
        TestHttpSampler http = new TestHttpSampler();
        manager.registerSampler(http);
        manager.registerSampler(new TestWebSampler());

        PluginRouter router = manager.buildRouter();
        assertThat(router.route("test-http")).isSameAs(http);
        assertThat(router.isPluginAvailable("test-web")).isTrue();
    }

    @Test
    @DisplayName("getRegistry contains PluginInfo for each registered sampler")
    void registryContainsPluginInfo() {
        manager.registerSampler(new TestHttpSampler());

        List<PluginInfo> all = manager.getRegistry().findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).id()).isEqualTo("test-http");
        assertThat(all.get(0).supportedPlatforms()).contains("HTTP_TESTING");
    }

    @Test
    @DisplayName("PluginInfo.fromSampler captures configSchema from sampler")
    void fromSamplerCapturesConfigSchema() {
        TestHttpSampler sampler = new TestHttpSampler();
        manager.registerSampler(sampler);

        List<PluginInfo> all = manager.getRegistry().findAll();
        assertThat(all).hasSize(1);
        // TestHttpSampler 默认返回空 schema（无类路径资源）
        assertThat(all.get(0).configSchema()).isNotNull();
    }

    @Test
    @DisplayName("unregisterSampler removes sampler and calls destroy()")
    void unregisterSamplerCallsDestroy() {
        TestHttpSampler sampler = new TestHttpSampler();
        manager.registerSampler(sampler);
        manager.unregisterSampler("test-http");

        assertThat(sampler.destroyed).isTrue();
        assertThat(manager.getSampler("test-http")).isEmpty();
        assertThat(manager.getRegistry().findById("test-http")).isEmpty();
    }

    @Test
    @DisplayName("registerSampler with null throws NullPointerException")
    void registerNullThrows() {
        assertThatThrownBy(() -> manager.registerSampler(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("loadPlugins does not throw when no ServiceLoader entries exist")
    void loadPluginsNoEntries() {
        // 测试类路径中无 META-INF/services 条目——应无错误完成
        manager.loadPlugins();
        assertThat(manager.getAllSamplers()).isEmpty();
    }

    @Test
    @DisplayName("registerSampler is idempotent: same instance is not re-initialized")
    void registerSameInstanceIsIdempotent() {
        TestHttpSampler sampler = new TestHttpSampler();
        manager.registerSampler(sampler);
        manager.registerSampler(sampler);
        manager.registerSampler(sampler);

        assertThat(sampler.initCount).isEqualTo(1);
        assertThat(sampler.destroyCount).isZero();
        assertThat(manager.getAllSamplers()).hasSize(1);
    }

    @Test
    @DisplayName("registerSampler same-class duplicate is skipped without re-initialize")
    void registerSameClassDuplicateSkipped() {
        TestHttpSampler first = new TestHttpSampler();
        TestHttpSampler second = new TestHttpSampler();
        manager.registerSampler(first);
        manager.registerSampler(second);

        assertThat(first.initCount).isEqualTo(1);
        assertThat(second.initCount).isZero();
        assertThat(first.destroyCount).isZero();
        assertThat(manager.getSampler("test-http")).contains(first);
    }

    @Test
    @DisplayName("registerSampler with different class destroys previous and registers new")
    void registerDifferentClassReplaces() {
        @PluginMetadata(name = "test-http", version = "1.0.0")
        class AltHttpSampler extends TestHttpSampler {
        }

        TestHttpSampler first = new TestHttpSampler();
        AltHttpSampler replacement = new AltHttpSampler();
        manager.registerSampler(first);
        manager.registerSampler(replacement);

        assertThat(first.destroyCount).isEqualTo(1);
        assertThat(replacement.initCount).isEqualTo(1);
        assertThat(manager.getSampler("test-http")).contains(replacement);
    }
}
