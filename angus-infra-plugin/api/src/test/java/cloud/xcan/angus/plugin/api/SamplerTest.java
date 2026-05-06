package cloud.xcan.angus.plugin.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.plugin.api.TestEvent.TestEventType;
import cloud.xcan.angus.spec.model.TestType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 核心 plugin-api DTO 和构建器模式的单元测试。
 */
class SamplerTest {

    // ── SampleContext 测试 ────────────────────────────────────────────────────

    @Test
    void sampleContext_builder_defaults() {
        SampleContext ctx = SampleContext.builder()
                .scriptId("script-01")
                .scenarioName("smoke")
                .stepName("GET /health")
                .build();

        assertEquals("script-01", ctx.getScriptId());
        assertEquals("smoke", ctx.getScenarioName());
        assertEquals("GET /health", ctx.getStepName());
        assertEquals(1, ctx.getIteration());
        assertEquals(1, ctx.getThreadId());
        assertEquals(TestType.PERFORMANCE, ctx.getTestType());
        assertNotNull(ctx.getVariables());
        assertNotNull(ctx.getProperties());
        assertTrue(ctx.getStartTime() > 0);
    }

    @Test
    void sampleContext_testType_override() {
        SampleContext ctx = SampleContext.builder()
                .testType(TestType.SECURITY)
                .build();
        assertEquals(TestType.SECURITY, ctx.getTestType());
    }

    @Test
    void sampleContext_variable_getAndSet() {
        SampleContext ctx = SampleContext.builder().build();

        assertNull(ctx.getVariable("token"));
        ctx.setVariable("token", "abc123");
        assertEquals("abc123", ctx.getVariable("token"));
    }

    @Test
    void sampleContext_property_getAndSet() {
        SampleContext ctx = SampleContext.builder().build();

        assertNull(ctx.getProperty("retries"));
        ctx.setProperty("retries", 3);
        assertEquals(3, ctx.getProperty("retries"));
    }

    @Test
    void sampleContext_setVariable_nullKeyThrows() {
        SampleContext ctx = SampleContext.builder().build();
        assertThrows(NullPointerException.class, () -> ctx.setVariable(null, "value"));
    }

    @Test
    void sampleContext_setProperty_nullKeyThrows() {
        SampleContext ctx = SampleContext.builder().build();
        assertThrows(NullPointerException.class, () -> ctx.setProperty(null, "value"));
    }

    @Test
    void sampleContext_stepConfig_and_environmentConfig() {
        Map<String, Object> config = Map.of("url", "https://example.com", "method", "GET");
        Map<String, String> envCfg = Map.of("BASE_URL", "https://staging.example.com");

        SampleContext ctx = SampleContext.builder()
                .stepConfig(config)
                .environment("staging")
                .environmentConfig(envCfg)
                .iteration(3)
                .threadId(7)
                .build();

        assertEquals("https://example.com", ctx.getStepConfig().get("url"));
        assertEquals("staging", ctx.getEnvironment());
        assertEquals("https://staging.example.com", ctx.getEnvironmentConfig().get("BASE_URL"));
        assertEquals(3, ctx.getIteration());
        assertEquals(7, ctx.getThreadId());
    }

    // ── SampleResult 测试 ─────────────────────────────────────────────────────

    @Test
    void sampleResult_builder_successResult() {
        SampleResult result = SampleResult.builder()
                .success(true)
                .statusCode(200)
                .responseBody("{\"ok\":true}")
                .responseTimeMs(123L)
                .startTime(1_000L)
                .endTime(1_123L)
                .build();

        assertTrue(result.isSuccess());
        assertEquals(200, result.getStatusCode());
        assertEquals("{\"ok\":true}", result.getResponseBody());
        assertEquals(123L, result.getResponseTimeMs());
        assertEquals(1_000L, result.getStartTime());
        assertEquals(1_123L, result.getEndTime());
        assertTrue(result.getAssertions().isEmpty());
        assertTrue(result.getExtractedVariables().isEmpty());
    }

    @Test
    void sampleResult_builder_failedResult() {
        SampleResult result = SampleResult.builder()
                .success(false)
                .statusCode(500)
                .errorMessage("Connection timed out")
                .errorCode("TIMEOUT")
                .build();

        assertFalse(result.isSuccess());
        assertEquals("Connection timed out", result.getErrorMessage());
        assertEquals("TIMEOUT", result.getErrorCode());
    }

    @Test
    void sampleResult_addAssertion() {
        AssertionResult pass = AssertionResult.passed("status-200", "STATUS_CODE");
        AssertionResult fail = AssertionResult.failed("body-check", "JSON_PATH",
                "active", "inactive", "Expected 'active' but got 'inactive'");

        SampleResult result = SampleResult.builder()
                .success(false)
                .addAssertion(pass)
                .addAssertion(fail)
                .build();

        assertEquals(2, result.getAssertions().size());
        assertTrue(result.getAssertions().get(0).passed());
        assertFalse(result.getAssertions().get(1).passed());
        assertFalse(result.allAssertionsPassed());
    }

    @Test
    void sampleResult_allAssertionsPassed_whenEmpty() {
        SampleResult result = SampleResult.builder().success(true).build();
        assertTrue(result.allAssertionsPassed(), "空断言列表应报告全部通过");
    }

    @Test
    void sampleResult_extractedVariables_and_metadata() {
        SampleResult result = SampleResult.builder()
                .success(true)
                .extractedVariables(Map.of("userId", "42"))
                .metadata(Map.of("traceId", "abc-xyz"))
                .build();

        assertEquals("42", result.getExtractedVariables().get("userId"));
        assertEquals("abc-xyz", result.getMetadata().get("traceId"));
    }

    @Test
    void sampleResult_timestamp_defaults_to_startTime() {
        SampleResult result = SampleResult.builder()
                .success(true)
                .startTime(5_000L)
                .build();
        assertEquals(5_000L, result.getTimestamp());
    }

    // ── AssertionResult 测试 ──────────────────────────────────────────────────

    @Test
    void assertionResult_passed_factory() {
        AssertionResult ar = AssertionResult.passed("check-status", "STATUS_CODE");
        assertTrue(ar.passed());
        assertEquals("check-status", ar.name());
        assertEquals("STATUS_CODE", ar.type());
        assertNull(ar.expected());
        assertNull(ar.actual());
        assertEquals("", ar.message());
    }

    @Test
    void assertionResult_failed_factory() {
        AssertionResult ar = AssertionResult.failed(
                "check-body", "REGEX", ".*success.*", "error", "Regex did not match");
        assertFalse(ar.passed());
        assertEquals(".*success.*", ar.expected());
        assertEquals("error", ar.actual());
        assertEquals("Regex did not match", ar.message());
    }

    // ── TestEvent 测试 ────────────────────────────────────────────────────────

    @Test
    void testEvent_builder_basic() {
        TestEvent event = TestEvent.builder()
                .type(TestEventType.STEP_END)
                .scriptId("s1")
                .scenarioName("login-flow")
                .stepName("POST /login")
                .duration(250L)
                .build();

        assertEquals(TestEventType.STEP_END, event.getType());
        assertEquals("s1", event.getScriptId());
        assertEquals("login-flow", event.getScenarioName());
        assertEquals("POST /login", event.getStepName());
        assertEquals(250L, event.getDuration());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testEvent_addMetadata() {
        TestEvent event = TestEvent.builder()
                .type(TestEventType.ERROR)
                .addMetadata("threadId", 3)
                .addMetadata("iteration", 10)
                .build();

        assertEquals(3, event.getMetadata("threadId"));
        assertEquals(10, event.getMetadata("iteration"));
    }

    @Test
    void testEvent_allEventTypes_exist() {
        TestEventType[] types = TestEventType.values();
        // 确保每个已声明的类型都能正常获取
        assertEquals(9, types.length,
                "预期有 9 个 TestEventType 值；如有新增类型请更新此测试");
    }

    // ── ScriptTemplate 测试 ───────────────────────────────────────────────────

    @Test
    void scriptTemplate_of_threeArg() {
        ScriptTemplate t = ScriptTemplate.of("Basic GET", "Simple GET request", "- name: step\n");
        assertEquals("Basic GET", t.name());
        assertEquals("Simple GET request", t.description());
        assertEquals("basic", t.category());
        assertEquals("- name: step\n", t.yamlContent());
    }

    @Test
    void scriptTemplate_of_twoArg_defaults() {
        ScriptTemplate t = ScriptTemplate.of("Quick Start", "- name: step\n");
        assertEquals("Quick Start", t.name());
        assertEquals("Quick Start", t.description());
        assertEquals("basic", t.category());
    }

    // ── AbstractSampler 测试 ──────────────────────────────────────────────────

    @Test
    void abstractSampler_annotationDriven_nameAndVersion() {
        @PluginMetadata(name = "test-sampler", version = "2.1.0")
        class AnnotatedSampler extends AbstractSampler {
            @Override
            public SampleResult sample(SampleContext context) { return createResult().build(); }
            @Override
            public List getSupportedPlatforms() { return List.of(); }
        }

        AbstractSampler s = new AnnotatedSampler();
        assertEquals("test-sampler", s.getName());
        assertEquals("2.1.0", s.getVersion());
        assertEquals("", s.getConfigSchema());
        assertTrue(s.getTemplates().isEmpty());
    }

    @Test
    void abstractSampler_noAnnotation_fallsBackToClassName() {
        class PlainSampler extends AbstractSampler {
            @Override
            public SampleResult sample(SampleContext context) { return createResult().build(); }
            @Override
            public List getSupportedPlatforms() { return List.of(); }
        }

        AbstractSampler s = new PlainSampler();
        assertEquals("PlainSampler", s.getName());
        assertEquals("0.0.0", s.getVersion());
    }

    @Test
    void abstractSampler_createFailedResult() {
        class MinimalSampler extends AbstractSampler {
            @Override
            public SampleResult sample(SampleContext context) { return null; }
            @Override
            public List getSupportedPlatforms() { return List.of(); }
        }

        AbstractSampler s = new MinimalSampler();
        SampleResult r = s.createFailedResult("timeout", new RuntimeException("timed out"));
        assertFalse(r.isSuccess());
        assertEquals("timeout", r.getErrorMessage());
        assertEquals("RuntimeException", r.getErrorCode());
        assertTrue(r.getStartTime() > 0);
        assertEquals(r.getStartTime(), r.getEndTime());
    }

    @Test
    void sampler_getConfigValidator_defaultReturnsNull() {
        class DefaultValidatorSampler extends AbstractSampler {
            @Override
            public SampleResult sample(SampleContext context) { return createResult().build(); }
            @Override
            public List getSupportedPlatforms() { return List.of(); }
        }

        Sampler s = new DefaultValidatorSampler();
        assertNull(s.getConfigValidator(), "默认 getConfigValidator() 应返回 null");
    }

    @Test
    void pluginConfigValidator_functionalInterface() {
        // PluginConfigValidator 是函数式接口，验证 lambda 可正常使用
        PluginConfigValidator validator = config -> {
            if (!config.containsKey("url")) {
                return List.of("Missing required field: url");
            }
            return List.of();
        };

        assertTrue(validator.validate(Map.of("url", "https://example.com")).isEmpty());
        assertFalse(validator.validate(Map.of("method", "GET")).isEmpty());
    }

    @Test
    void abstractSampler_getConfigSchema_returnsEmptyWhenNoResource() {
        class NoResourceSampler extends AbstractSampler {
            @Override
            public SampleResult sample(SampleContext context) { return createResult().build(); }
            @Override
            public List getSupportedPlatforms() { return List.of(); }
        }

        AbstractSampler s = new NoResourceSampler();
        assertEquals("", s.getConfigSchema(),
                "无类路径资源时应返回空字符串");
    }
}
