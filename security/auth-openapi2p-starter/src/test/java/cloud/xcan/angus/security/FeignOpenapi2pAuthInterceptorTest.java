package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import feign.RequestTemplate;
import java.lang.reflect.Field;
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

  @Mock
  private RequestTemplate template;

  private FeignOpenapi2pAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    when(properties.getRequestPathPrefix()).thenReturn("/openapi2p");
    interceptor = new FeignOpenapi2pAuthInterceptor(
        clientSignOpenapi2pRemote, properties, environment);
  }

  @Nested
  @DisplayName("apply()")
  class Apply {

    @Test
    @DisplayName("skips non-matching paths without adding header")
    void skipsNonMatchingPath() {
      when(template.path()).thenReturn("/api/users");
      when(properties.shouldIntercept("/api/users")).thenReturn(false);

      interceptor.apply(template);

      verify(template, never()).header(anyString(), anyString());
    }

    @Test
    @DisplayName("skips when shouldIntercept returns false for null path")
    void skipsNullPath() {
      when(template.path()).thenReturn(null);
      when(properties.shouldIntercept(null)).thenReturn(false);

      interceptor.apply(template);

      verify(template, never()).header(anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("clearCache()")
  class ClearCache {

    @Test
    @DisplayName("clears cached token and resets timestamp")
    void clearsCachedToken() throws Exception {
      // Pre-populate cache via reflection
      Field tokenField = FeignOpenapi2pAuthInterceptor.class
          .getDeclaredField("cachedOpenapi2pToken");
      tokenField.setAccessible(true);
      tokenField.set(interceptor, "Bearer some-token");

      Field timeField = FeignOpenapi2pAuthInterceptor.class
          .getDeclaredField("cachedTokenTime");
      timeField.setAccessible(true);
      timeField.set(interceptor, System.currentTimeMillis());

      // Verify token was set
      assertThat(tokenField.get(interceptor)).isEqualTo("Bearer some-token");

      interceptor.clearCache();

      assertThat(tokenField.get(interceptor)).isNull();
      assertThat((long) timeField.get(interceptor)).isEqualTo(0L);
    }

    @Test
    @DisplayName("clearCache is idempotent when already empty")
    void clearCacheIdempotent() throws Exception {
      // Call clearCache on already empty state
      interceptor.clearCache();

      Field tokenField = FeignOpenapi2pAuthInterceptor.class
          .getDeclaredField("cachedOpenapi2pToken");
      tokenField.setAccessible(true);
      assertThat(tokenField.get(interceptor)).isNull();
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
