package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.security.model.cache.LocalTokenStore;
import cloud.xcan.angus.security.model.cache.TokenStore;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;

@DisplayName("FeignOpenapi2pAuthInterceptor Tests")
@ExtendWith(MockitoExtension.class)
class FeignOpenapi2pAuthInterceptorTest {

  @Mock
  private ClientSignOpenapi2pRemote clientSignOpenapi2pRemote;

  @Mock
  private Openapi2pAuthProperties properties;

  @Mock
  private ConfigurableEnvironment environment;

  private TokenStore tokenStore;

  private FeignOpenapi2pAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    when(properties.getRequestPathPrefix()).thenReturn("/openapi2p");
    lenient().when(properties.getCacheType()).thenReturn(
        cloud.xcan.angus.security.model.cache.CacheType.LOCAL);
    lenient().when(properties.getCacheKey()).thenReturn("auth:openapi2p:token");

    tokenStore = new LocalTokenStore();
    interceptor = new FeignOpenapi2pAuthInterceptor(
        clientSignOpenapi2pRemote, properties, environment, tokenStore);
  }

  @Nested
  @DisplayName("apply()")
  class Apply {

    @Test
    @DisplayName("skips non-matching paths without adding header")
    void skipsNonMatchingPath() {
      when(properties.shouldIntercept("/api/users")).thenReturn(false);

      RequestTemplate template = new RequestTemplate();
      template.uri("/api/users");

      interceptor.apply(template);

      assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    @DisplayName("skips when path is unset/empty and shouldIntercept is false")
    void skipsUnsetPath() {
      lenient().when(properties.shouldIntercept(nullable(String.class))).thenReturn(false);

      RequestTemplate template = new RequestTemplate();

      interceptor.apply(template);

      assertThat(template.headers()).doesNotContainKey("Authorization");
    }
  }

  @Nested
  @DisplayName("clearCache()")
  class ClearCache {

    @Test
    @DisplayName("clears cached token from token store")
    void clearsCachedToken() {
      // Pre-populate cache via the token store
      tokenStore.store("auth:openapi2p:token", "Bearer some-token", 900);

      // Verify token was stored
      assertThat(tokenStore.exists("auth:openapi2p:token")).isTrue();

      interceptor.clearCache();

      assertThat(tokenStore.exists("auth:openapi2p:token")).isFalse();
    }

    @Test
    @DisplayName("clearCache is idempotent when already empty")
    void clearCacheIdempotent() {
      // Call clearCache on already empty state
      interceptor.clearCache();

      assertThat(tokenStore.exists("auth:openapi2p:token")).isFalse();
    }
  }

  @Nested
  @DisplayName("Construction")
  class Construction {

    @Test
    @DisplayName("initializes without errors")
    void initializesCorrectly() {
      // The interceptor should be non-null after construction
      assertThat(interceptor).isNotNull();
    }
  }
}
