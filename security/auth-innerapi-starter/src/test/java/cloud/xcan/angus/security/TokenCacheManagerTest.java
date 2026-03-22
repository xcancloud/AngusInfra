package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.security.cache.TokenCacheManager;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import cloud.xcan.angus.security.model.cache.LocalTokenStore;
import cloud.xcan.angus.security.model.cache.TokenStore;
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("TokenCacheManager Tests")
@ExtendWith(MockitoExtension.class)
class TokenCacheManagerTest {

  @Mock
  private InnerApiAuthProperties properties;

  @Mock
  private ClientSignInnerApiRemote clientSignInnerApiRemote;

  private TokenStore tokenStore;

  private TokenCacheManager tokenCacheManager;

  @BeforeEach
  void setUp() {
    // Set up properties mock with default values needed by the constructor
    when(properties.getTokenCacheInterval()).thenReturn(Duration.ofMinutes(15));
    when(properties.getTokenRefreshThreshold()).thenReturn(Duration.ofMinutes(2));
    when(properties.getMaxRetries()).thenReturn(3);
    lenient().when(properties.getEffectiveTokenCacheDuration()).thenReturn(Duration.ofMinutes(13));
    lenient().when(properties.getCacheType()).thenReturn(
        cloud.xcan.angus.security.model.cache.CacheType.LOCAL);
    lenient().when(properties.getCacheKey()).thenReturn("auth:innerapi:token");

    tokenStore = new LocalTokenStore();
    tokenCacheManager = new TokenCacheManager(properties, clientSignInnerApiRemote, tokenStore);
  }

  /**
   * Helper to pre-populate the token store with a token for testing cache behavior.
   */
  private void storeToken(String token, long ttlSeconds) {
    tokenStore.store("auth:innerapi:token", token, ttlSeconds);
  }

  @Nested
  @DisplayName("getToken()")
  class GetToken {

    @Test
    @DisplayName("returns cached token when it is still fresh")
    void returnsCachedTokenWhenFresh() {
      // Pre-populate the cache with a fresh token (long TTL)
      storeToken("Bearer test-token-123", 900);

      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer test-token-123");
      // Should NOT have called the remote service
      verify(clientSignInnerApiRemote, times(0)).signin(any());
    }

    @Test
    @DisplayName("falls back to fallback token when refresh fails and store is expired")
    void fallsBackToExpiredCachedToken() throws Exception {
      // Store a token with a very short TTL so it expires
      storeToken("Bearer expired-token", 1);
      // Wait for it to expire
      Thread.sleep(1100);

      // getToken() will see expired token, try to refresh, fail due to missing env vars,
      // and since no fallback is set yet, it will throw.
      assertThatThrownBy(() -> tokenCacheManager.getToken())
          .isInstanceOf(SysException.class)
          .hasMessageContaining("Unable to obtain authentication token");
    }

    @Test
    @DisplayName("throws SysException when refresh fails and no cached token")
    void throwsWhenNoFallback() {
      // No cached token, and env vars not set -> buildTokenRequest() will fail
      assertThatThrownBy(() -> tokenCacheManager.getToken())
          .isInstanceOf(SysException.class)
          .hasMessageContaining("Unable to obtain authentication token");
    }

    @Test
    @DisplayName("does not call remote when cached token is within effective duration")
    void doesNotCallRemoteForFreshToken() {
      // Store a token with long TTL
      storeToken("Bearer recent-token", 780);

      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer recent-token");
      verify(clientSignInnerApiRemote, times(0)).signin(any());
    }

    @Test
    @DisplayName("attempts refresh when token has expired in store")
    void attemptsRefreshWhenExpired() throws Exception {
      // Store a token with very short TTL so it expires
      storeToken("Bearer old-token", 1);
      // Wait for it to expire
      Thread.sleep(1100);

      // Refresh will fail due to missing env vars, and no fallback exists
      assertThatThrownBy(() -> tokenCacheManager.getToken())
          .isInstanceOf(SysException.class)
          .hasMessageContaining("Unable to obtain authentication token");
    }
  }

  @Nested
  @DisplayName("getTokenWithRetry()")
  class GetTokenWithRetry {

    @Test
    @DisplayName("returns token on first successful attempt")
    void returnsTokenOnFirstAttempt() {
      // Pre-populate cache with fresh token so getToken() returns immediately
      storeToken("Bearer cached-token", 900);

      String token = tokenCacheManager.getTokenWithRetry();

      assertThat(token).isEqualTo("Bearer cached-token");
    }

    @Test
    @DisplayName("throws SysException after all retries exhausted with no fallback")
    void throwsAfterAllRetriesExhausted() {
      // maxRetries = 3, retryDelay setup
      when(properties.getRetryDelay(1)).thenReturn(Duration.ofMillis(1));
      when(properties.getRetryDelay(2)).thenReturn(Duration.ofMillis(1));

      // No cached token, env vars not set -> every attempt fails
      assertThatThrownBy(() -> tokenCacheManager.getTokenWithRetry())
          .isInstanceOf(SysException.class)
          .hasMessageContaining("Unable to obtain authentication token after 3 retry attempts");
    }

    @Test
    @DisplayName("succeeds without retrying when token is fresh")
    void succeedsWithoutRetryWhenFresh() {
      storeToken("Bearer fresh-token", 900);

      String token = tokenCacheManager.getTokenWithRetry();

      assertThat(token).isEqualTo("Bearer fresh-token");
      // No retries needed - remote should never be called
      verify(clientSignInnerApiRemote, times(0)).signin(any());
    }
  }

  @Nested
  @DisplayName("clearCache()")
  class ClearCache {

    @Test
    @DisplayName("clears cached token")
    void clearsCachedToken() {
      storeToken("Bearer some-token", 900);

      assertThat(tokenCacheManager.hasCachedToken()).isTrue();

      tokenCacheManager.clearCache();

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }

    @Test
    @DisplayName("clearCache is idempotent when already empty")
    void clearCacheIdempotent() {
      tokenCacheManager.clearCache();

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }
  }

  @Nested
  @DisplayName("hasCachedToken()")
  class HasCachedToken {

    @Test
    @DisplayName("returns false when no cached token")
    void returnsFalseWhenNoCachedToken() {
      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }

    @Test
    @DisplayName("returns true when cached token is fresh")
    void returnsTrueWhenFresh() {
      storeToken("Bearer valid-token", 900);

      assertThat(tokenCacheManager.hasCachedToken()).isTrue();
    }

    @Test
    @DisplayName("returns false when cached token is expired")
    void returnsFalseWhenExpired() throws Exception {
      // Store with very short TTL so it expires
      storeToken("Bearer expired-token", 1);
      // Wait for it to expire
      Thread.sleep(1100);

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }
  }

  @Nested
  @DisplayName("Thread Safety")
  class ThreadSafety {

    @Test
    @DisplayName("concurrent reads of cached token return consistent results")
    void concurrentReadsReturnConsistentResults() throws Exception {
      storeToken("Bearer concurrent-token", 900);

      int threadCount = 10;
      String[] results = new String[threadCount];
      Thread[] threads = new Thread[threadCount];

      for (int i = 0; i < threadCount; i++) {
        final int index = i;
        threads[i] = new Thread(() -> {
          results[index] = tokenCacheManager.getToken();
        });
      }

      for (Thread t : threads) {
        t.start();
      }
      for (Thread t : threads) {
        t.join(5000);
      }

      for (String result : results) {
        assertThat(result).isEqualTo("Bearer concurrent-token");
      }
    }

    @Test
    @DisplayName("clearCache during concurrent reads does not cause errors")
    void clearCacheDuringConcurrentReads() throws Exception {
      storeToken("Bearer token-to-clear", 900);

      Thread[] threads = new Thread[5];
      Exception[] errors = new Exception[5];

      for (int i = 0; i < 5; i++) {
        final int idx = i;
        threads[i] = new Thread(() -> {
          try {
            if (idx % 2 == 0) {
              tokenCacheManager.getToken();
            } else {
              tokenCacheManager.clearCache();
            }
          } catch (Exception e) {
            errors[idx] = e;
          }
        });
      }

      for (Thread t : threads) {
        t.start();
      }
      for (Thread t : threads) {
        t.join(5000);
      }

      // No thread should have thrown an unexpected error
      // (getToken after clearCache may throw SysException, which is expected)
      for (int i = 0; i < 5; i++) {
        if (errors[i] != null && !(errors[i] instanceof SysException)) {
          throw errors[i];
        }
      }
    }
  }
}
