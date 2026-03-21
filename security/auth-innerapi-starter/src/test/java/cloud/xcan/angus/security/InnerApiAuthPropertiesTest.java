package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InnerApiAuthProperties Tests")
class InnerApiAuthPropertiesTest {

  private InnerApiAuthProperties properties;

  @BeforeEach
  void setUp() {
    properties = new InnerApiAuthProperties();
  }

  @Nested
  @DisplayName("Default Values")
  class DefaultValues {

    @Test
    @DisplayName("enabled defaults to true")
    void enabledDefaultsToTrue() {
      assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("requestPathPrefixes defaults to [/innerapi]")
    void requestPathPrefixesDefaultsToInnerapi() {
      assertThat(properties.getRequestPathPrefixes())
          .containsExactly("/innerapi");
    }

    @Test
    @DisplayName("tokenCacheInterval defaults to 15 minutes")
    void tokenCacheIntervalDefaultsTo15Minutes() {
      assertThat(properties.getTokenCacheInterval())
          .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("tokenRefreshThreshold defaults to 2 minutes")
    void tokenRefreshThresholdDefaultsTo2Minutes() {
      assertThat(properties.getTokenRefreshThreshold())
          .isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    @DisplayName("maxRetries defaults to 3")
    void maxRetriesDefaultsTo3() {
      assertThat(properties.getMaxRetries()).isEqualTo(3);
    }

    @Test
    @DisplayName("retryInterval defaults to 1 second")
    void retryIntervalDefaultsTo1Second() {
      assertThat(properties.getRetryInterval())
          .isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("connectionTimeout defaults to 5 seconds")
    void connectionTimeoutDefaultsTo5Seconds() {
      assertThat(properties.getConnectionTimeout())
          .isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("readTimeout defaults to 10 seconds")
    void readTimeoutDefaultsTo10Seconds() {
      assertThat(properties.getReadTimeout())
          .isEqualTo(Duration.ofSeconds(10));
    }
  }

  @Nested
  @DisplayName("Constants")
  class Constants {

    @Test
    @DisplayName("BEARER_TOKEN_TYPE is 'Bearer'")
    void bearerTokenType() {
      assertThat(InnerApiAuthProperties.BEARER_TOKEN_TYPE).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("DEFAULT_INNER_API_PATH_PREFIX is '/innerapi'")
    void defaultPathPrefix() {
      assertThat(InnerApiAuthProperties.DEFAULT_INNER_API_PATH_PREFIX)
          .isEqualTo("/innerapi");
    }

    @Test
    @DisplayName("INNER_API_TOKEN_CLIENT_SCOPE is 'innerapi'")
    void clientScope() {
      assertThat(InnerApiAuthProperties.INNER_API_TOKEN_CLIENT_SCOPE)
          .isEqualTo("innerapi");
    }

    @Test
    @DisplayName("Environment variable names are correctly defined")
    void envVariableNames() {
      assertThat(InnerApiAuthProperties.CLIENT_ID_ENV_PROPERTY)
          .isEqualTo("OAUTH2_INNER_API_CLIENT_ID");
      assertThat(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY)
          .isEqualTo("OAUTH2_INNER_API_CLIENT_SECRET");
      assertThat(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY_LEGACY)
          .isEqualTo("INNER_API_CLIENT_SECRET");
    }
  }

  @Nested
  @DisplayName("shouldIntercept()")
  class ShouldIntercept {

    @Test
    @DisplayName("returns true for path matching default prefix /innerapi")
    void matchesDefaultPrefix() {
      assertThat(properties.shouldIntercept("/innerapi/users")).isTrue();
    }

    @Test
    @DisplayName("returns true for exact default prefix /innerapi")
    void matchesExactPrefix() {
      assertThat(properties.shouldIntercept("/innerapi")).isTrue();
    }

    @Test
    @DisplayName("returns false for non-matching path")
    void doesNotMatchNonMatchingPath() {
      assertThat(properties.shouldIntercept("/api/users")).isFalse();
    }

    @Test
    @DisplayName("returns false for null path")
    void returnsFalseForNullPath() {
      assertThat(properties.shouldIntercept(null)).isFalse();
    }

    @Test
    @DisplayName("returns false when disabled")
    void returnsFalseWhenDisabled() {
      properties.setEnabled(false);
      assertThat(properties.shouldIntercept("/innerapi/users")).isFalse();
    }

    @Test
    @DisplayName("returns true for any of multiple configured prefixes")
    void matchesMultiplePrefixes() {
      properties.setRequestPathPrefixes(
          new ArrayList<>(Arrays.asList("/innerapi", "/system-api", "/admin-api")));
      assertThat(properties.shouldIntercept("/innerapi/test")).isTrue();
      assertThat(properties.shouldIntercept("/system-api/config")).isTrue();
      assertThat(properties.shouldIntercept("/admin-api/users")).isTrue();
      assertThat(properties.shouldIntercept("/public/health")).isFalse();
    }
  }

  @Nested
  @DisplayName("getEffectiveTokenCacheDuration()")
  class GetEffectiveTokenCacheDuration {

    @Test
    @DisplayName("returns cacheInterval minus refreshThreshold with defaults")
    void returnsCorrectDurationWithDefaults() {
      // 15min - 2min = 13min
      assertThat(properties.getEffectiveTokenCacheDuration())
          .isEqualTo(Duration.ofMinutes(13));
    }

    @Test
    @DisplayName("returns correct duration with custom values")
    void returnsCorrectDurationWithCustomValues() {
      properties.setTokenCacheInterval(Duration.ofMinutes(30));
      properties.setTokenRefreshThreshold(Duration.ofMinutes(5));
      // 30min - 5min = 25min
      assertThat(properties.getEffectiveTokenCacheDuration())
          .isEqualTo(Duration.ofMinutes(25));
    }
  }

  @Nested
  @DisplayName("getRetryDelay()")
  class GetRetryDelay {

    @Test
    @DisplayName("returns linear backoff delay based on attempt number")
    void returnsLinearBackoffDelay() {
      // retryInterval = 1s, delay = retryInterval * attemptNumber
      assertThat(properties.getRetryDelay(1)).isEqualTo(Duration.ofSeconds(1));
      assertThat(properties.getRetryDelay(2)).isEqualTo(Duration.ofSeconds(2));
      assertThat(properties.getRetryDelay(3)).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("returns correct delay with custom retry interval")
    void returnsCorrectDelayWithCustomInterval() {
      properties.setRetryInterval(Duration.ofMillis(500));
      assertThat(properties.getRetryDelay(1)).isEqualTo(Duration.ofMillis(500));
      assertThat(properties.getRetryDelay(2)).isEqualTo(Duration.ofMillis(1000));
      assertThat(properties.getRetryDelay(3)).isEqualTo(Duration.ofMillis(1500));
    }
  }

  @Nested
  @DisplayName("validate()")
  class Validate {

    @Test
    @DisplayName("succeeds with default configuration")
    void succeedsWithDefaults() {
      // Should not throw
      properties.validate();
    }

    @Test
    @DisplayName("fails when tokenCacheInterval is less than 1 minute")
    void failsWhenCacheIntervalTooShort() {
      properties.setTokenCacheInterval(Duration.ofSeconds(30));
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("tokenCacheInterval must be at least 1 minute");
    }

    @Test
    @DisplayName("fails when tokenRefreshThreshold >= tokenCacheInterval")
    void failsWhenRefreshThresholdTooLarge() {
      properties.setTokenRefreshThreshold(Duration.ofMinutes(15));
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("tokenRefreshThreshold must be less than tokenCacheInterval");
    }

    @Test
    @DisplayName("fails when maxRetries is negative")
    void failsWhenMaxRetriesNegative() {
      properties.setMaxRetries(-1);
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("maxRetries must be >= 0");
    }

    @Test
    @DisplayName("fails when retryInterval is less than 100ms")
    void failsWhenRetryIntervalTooShort() {
      properties.setRetryInterval(Duration.ofMillis(50));
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("retryInterval must be at least 100ms");
    }

    @Test
    @DisplayName("fails when connectionTimeout is less than 500ms")
    void failsWhenConnectionTimeoutTooShort() {
      properties.setConnectionTimeout(Duration.ofMillis(200));
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("connectionTimeout must be at least 500ms");
    }

    @Test
    @DisplayName("fails when readTimeout is less than 1000ms")
    void failsWhenReadTimeoutTooShort() {
      properties.setReadTimeout(Duration.ofMillis(500));
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("readTimeout must be at least 1000ms");
    }

    @Test
    @DisplayName("fails when requestPathPrefixes is empty")
    void failsWhenPathPrefixesEmpty() {
      properties.setRequestPathPrefixes(new ArrayList<>());
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("requestPathPrefixes must not be empty");
    }

    @Test
    @DisplayName("fails when requestPathPrefixes is null")
    void failsWhenPathPrefixesNull() {
      properties.setRequestPathPrefixes(null);
      assertThatThrownBy(() -> properties.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("requestPathPrefixes must not be empty");
    }

    @Test
    @DisplayName("succeeds with maxRetries set to 0")
    void succeedsWithZeroRetries() {
      properties.setMaxRetries(0);
      // Should not throw
      properties.validate();
    }
  }
}
