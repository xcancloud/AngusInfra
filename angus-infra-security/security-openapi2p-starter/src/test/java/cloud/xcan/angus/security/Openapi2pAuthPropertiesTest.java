package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Openapi2pAuthProperties Tests")
class Openapi2pAuthPropertiesTest {

  private Openapi2pAuthProperties properties;

  @BeforeEach
  void setUp() {
    properties = new Openapi2pAuthProperties();
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
    @DisplayName("requestPathPrefix defaults to /openapi2p")
    void requestPathPrefixDefault() {
      assertThat(properties.getRequestPathPrefix()).isEqualTo("/openapi2p");
    }

    @Test
    @DisplayName("tokenCacheInterval defaults to 15 minutes")
    void tokenCacheIntervalDefault() {
      assertThat(properties.getTokenCacheInterval())
          .isEqualTo(Duration.ofMinutes(15));
    }
  }

  @Nested
  @DisplayName("Constants")
  class Constants {

    @Test
    @DisplayName("CLIENT_ID_ENV_PROPERTY is correctly defined")
    void clientIdEnvProperty() {
      assertThat(Openapi2pAuthProperties.CLIENT_ID_ENV_PROPERTY)
          .isEqualTo("OAUTH2_OPENAPI2P_CLIENT_ID");
    }

    @Test
    @DisplayName("CLIENT_SECRET_ENV_PROPERTY is correctly defined")
    void clientSecretEnvProperty() {
      assertThat(Openapi2pAuthProperties.CLIENT_SECRET_ENV_PROPERTY)
          .isEqualTo("OAUTH2_OPENAPI2P_CLIENT_SECRET");
    }

    @Test
    @DisplayName("DEFAULT_OPENAPI2P_PATH_PREFIX is /openapi2p")
    void defaultPathPrefix() {
      assertThat(Openapi2pAuthProperties.DEFAULT_OPENAPI2P_PATH_PREFIX)
          .isEqualTo("/openapi2p");
    }
  }

  @Nested
  @DisplayName("shouldIntercept()")
  class ShouldIntercept {

    @Test
    @DisplayName("returns true for path starting with /openapi2p")
    void matchesOpenapi2pPath() {
      assertThat(properties.shouldIntercept("/openapi2p/v1/resources")).isTrue();
    }

    @Test
    @DisplayName("returns true for exact /openapi2p path")
    void matchesExactPath() {
      assertThat(properties.shouldIntercept("/openapi2p")).isTrue();
    }

    @Test
    @DisplayName("returns false for non-matching path")
    void doesNotMatchNonMatchingPath() {
      assertThat(properties.shouldIntercept("/api/users")).isFalse();
    }

    @Test
    @DisplayName("returns false for null path")
    void returnsFalseForNull() {
      assertThat(properties.shouldIntercept(null)).isFalse();
    }

    @Test
    @DisplayName("returns false when disabled")
    void returnsFalseWhenDisabled() {
      properties.setEnabled(false);
      assertThat(properties.shouldIntercept("/openapi2p/test")).isFalse();
    }

    @Test
    @DisplayName("works with custom path prefix")
    void worksWithCustomPrefix() {
      properties.setRequestPathPrefix("/custom-api");
      assertThat(properties.shouldIntercept("/custom-api/data")).isTrue();
      assertThat(properties.shouldIntercept("/openapi2p/data")).isFalse();
    }
  }

  @Nested
  @DisplayName("Setters")
  class Setters {

    @Test
    @DisplayName("can set and get enabled state")
    void setEnabled() {
      properties.setEnabled(false);
      assertThat(properties.isEnabled()).isFalse();
      properties.setEnabled(true);
      assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("can set and get custom token cache interval")
    void setTokenCacheInterval() {
      properties.setTokenCacheInterval(Duration.ofMinutes(30));
      assertThat(properties.getTokenCacheInterval()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("can set and get custom request path prefix")
    void setRequestPathPrefix() {
      properties.setRequestPathPrefix("/new-prefix");
      assertThat(properties.getRequestPathPrefix()).isEqualTo("/new-prefix");
    }
  }
}
