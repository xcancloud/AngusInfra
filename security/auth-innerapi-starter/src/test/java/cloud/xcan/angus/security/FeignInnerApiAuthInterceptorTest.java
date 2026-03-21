package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.security.cache.TokenCacheManager;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FeignInnerApiAuthInterceptor Tests")
@ExtendWith(MockitoExtension.class)
class FeignInnerApiAuthInterceptorTest {

  @Mock
  private TokenCacheManager tokenCacheManager;

  @Mock
  private InnerApiAuthProperties properties;

  private FeignInnerApiAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new FeignInnerApiAuthInterceptor(tokenCacheManager, properties);
  }

  @Nested
  @DisplayName("apply()")
  class Apply {

    @Test
    @DisplayName("adds Authorization header for matching path")
    void addsAuthorizationHeaderForMatchingPath() {
      when(properties.isEnabled()).thenReturn(true);
      when(properties.shouldIntercept("/innerapi/users")).thenReturn(true);
      when(tokenCacheManager.getTokenWithRetry()).thenReturn("Bearer test-token");

      RequestTemplate template = new RequestTemplate();
      template.uri("/innerapi/users");

      interceptor.apply(template);

      assertThat(template.headers().get("Authorization"))
          .isNotNull()
          .contains("Bearer test-token");
    }

    @Test
    @DisplayName("skips non-matching path without adding header")
    void skipsNonMatchingPath() {
      when(properties.isEnabled()).thenReturn(true);
      when(properties.shouldIntercept("/api/public/health")).thenReturn(false);

      RequestTemplate template = new RequestTemplate();
      template.uri("/api/public/health");

      interceptor.apply(template);

      assertThat(template.headers()).doesNotContainKey("Authorization");
      verify(tokenCacheManager, never()).getTokenWithRetry();
    }

    @Test
    @DisplayName("skips when disabled")
    void skipsWhenDisabled() {
      when(properties.isEnabled()).thenReturn(false);

      RequestTemplate template = new RequestTemplate();
      template.uri("/innerapi/data");

      interceptor.apply(template);

      assertThat(template.headers()).doesNotContainKey("Authorization");
      verify(tokenCacheManager, never()).getTokenWithRetry();
    }

    @Test
    @DisplayName("throws RuntimeException when token retrieval fails")
    void throwsWhenTokenRetrievalFails() {
      when(properties.isEnabled()).thenReturn(true);
      when(properties.shouldIntercept("/innerapi/data")).thenReturn(true);
      when(tokenCacheManager.getTokenWithRetry())
          .thenThrow(new RuntimeException("Token retrieval failed"));

      RequestTemplate template = new RequestTemplate();
      template.uri("/innerapi/data");

      assertThatThrownBy(() -> interceptor.apply(template))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Inner API authentication failed for path: /innerapi/data");
    }

    @Test
    @DisplayName("does not add header when path is null and shouldIntercept returns false")
    void handlesNullPath() {
      when(properties.isEnabled()).thenReturn(true);
      lenient().when(properties.shouldIntercept(null)).thenReturn(false);

      RequestTemplate template = new RequestTemplate();
      // template path defaults to empty string

      interceptor.apply(template);

      assertThat(template.headers()).doesNotContainKey("Authorization");
    }
  }
}
