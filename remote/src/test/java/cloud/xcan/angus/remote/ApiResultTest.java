package cloud.xcan.angus.remote;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApiResult Unit Tests")
class ApiResultTest {

  private ApiResult<String> result;

  @BeforeEach
  void setUp() {
    result = new ApiResult<>();
  }

  @Test
  @DisplayName("should create successful response with default constructor")
  void testDefaultConstructor() {
    ApiResult<Void> response = new ApiResult<>();

    assertThat(response.getCode()).isEqualTo("S");
    assertThat(response.getMessage()).isNotEmpty();
    assertThat(response.getData()).isNull();
  }

  @Test
  @DisplayName("should create successful response with data")
  void testConstructorWithData() {
    String data = "test data";
    ApiResult<String> response = new ApiResult<>(data);

    assertThat(response.getCode()).isEqualTo("S");
    assertThat(response.getData()).isEqualTo(data);
  }

  @Test
  @DisplayName("should create response with code and message")
  void testConstructorWithCodeAndMessage() {
    ApiResult<Void> response = new ApiResult<>("E1", "Business error occurred");

    assertThat(response.getCode()).isEqualTo("E1");
    assertThat(response.getMessage()).isEqualTo("Business error occurred");
    assertThat(response.getData()).isNull();
  }

  @Test
  @DisplayName("should support fluent API with setters")
  void testFluentApi() {
    ApiResult<String> response = new ApiResult<String>()
        .setCode("S")
        .setMessage("Success")
        .setData("test")
        .setExtensions(Map.of("key", "value"));

    assertThat(response.getCode()).isEqualTo("S");
    assertThat(response.getData()).isEqualTo("test");
    assertThat(response.getExtensions()).containsEntry("key", "value");
  }

  @Test
  @DisplayName("should initialize extensions map lazily")
  void testExtensionsLazy() {
    ApiResult<String> response = new ApiResult<>("S", "OK", "data");

    assertThat(response.getExtensions()).isNull();

    response.setExtensions(new HashMap<>());
    assertThat(response.getExtensions()).isNotNull();
  }

  @Test
  @DisplayName("should support extension metadata")
  void testExtensionMetadata() {
    Map<String, Object> extensions = new HashMap<>();
    extensions.put("traceId", "abc-123-def");
    extensions.put("requestId", "req-001");

    ApiResult<String> response = new ApiResult<>("S", "OK", "data", extensions);

    assertThat(response.getExtensions()).containsEntry("traceId", "abc-123-def")
        .containsEntry("requestId", "req-001");
  }

  @Test
  @DisplayName("should include timestamp in response")
  void testTimestampIncluded() {
    ApiResult<String> response = new ApiResult<>("S", "OK", "data");

    assertThat(response.getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("should support custom error codes")
  void testCustomErrorCodes() {
    ApiResult<Void> protocolError = new ApiResult<>("E0", "Protocol error");
    assertThat(protocolError.getCode()).isEqualTo("E0");

    ApiResult<Void> businessError = new ApiResult<>("E1", "Business error");
    assertThat(businessError.getCode()).isEqualTo("E1");

    ApiResult<Void> systemError = new ApiResult<>("E2", "System error");
    assertThat(systemError.getCode()).isEqualTo("E2");

    ApiResult<Void> quotaError = new ApiResult<>("E3", "Quota exceeded");
    assertThat(quotaError.getCode()).isEqualTo("E3");
  }

  @Test
  @DisplayName("should handle null data gracefully")
  void testNullDataHandling() {
    ApiResult<String> response = new ApiResult<>("S", "OK", null);

    assertThat(response.getData()).isNull();
    assertThat(response.getCode()).isEqualTo("S");
  }

  @Test
  @DisplayName("should support generic types with various data")
  void testGenericTypes() {
    // Test with different types
    ApiResult<Long> longResult = new ApiResult<>(123L);
    assertThat(longResult.getData()).isEqualTo(123L);

    ApiResult<Integer> intResult = new ApiResult<>(456);
    assertThat(intResult.getData()).isEqualTo(456);

    ApiResult<Boolean> boolResult = new ApiResult<>(true);
    assertThat(boolResult.getData()).isTrue();
  }

  @Test
  @DisplayName("should implement toString")
  void testToString() {
    ApiResult<String> response = new ApiResult<>("S", "OK", "data");

    String stringRepresentation = response.toString();
    assertThat(stringRepresentation).contains("ApiResult");
  }

  @Test
  @DisplayName("should validate extension value types")
  void testExtensionValueValidation() {
    Map<String, Object> extensions = new HashMap<>();
    extensions.put("stringValue", "test");
    extensions.put("numberValue", 123);
    extensions.put("booleanValue", true);
    extensions.put("nullValue", null);

    ApiResult<String> response = new ApiResult<>("S", "OK", "data", extensions);

    assertThat(response.getExtensions()).hasSize(4);
  }
}
