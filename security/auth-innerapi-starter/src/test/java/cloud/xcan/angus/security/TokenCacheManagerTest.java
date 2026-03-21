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
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import java.lang.reflect.Field;
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

  private TokenCacheManager tokenCacheManager;

  @BeforeEach
  void setUp() {
    // Set up properties mock with default values needed by the constructor
    when(properties.getTokenCacheInterval()).thenReturn(Duration.ofMinutes(15));
    when(properties.getTokenRefreshThreshold()).thenReturn(Duration.ofMinutes(2));
    when(properties.getMaxRetries()).thenReturn(3);
    lenient().when(properties.getEffectiveTokenCacheDuration()).thenReturn(Duration.ofMinutes(13));

    tokenCacheManager = new TokenCacheManager(properties, clientSignInnerApiRemote);
  }

  /**
   * Helper to set the private cachedToken field via reflection for testing cache behavior.
   */
  private void setCachedToken(String token) throws Exception {
    Field field = TokenCacheManager.class.getDeclaredField("cachedToken");
    field.setAccessible(true);
    field.set(tokenCacheManager, token);
  }

  /**
   * Helper to set the private cachedTokenTime field via reflection.
   */
  private void setCachedTokenTime(long time) throws Exception {
    Field field = TokenCacheManager.class.getDeclaredField("cachedTokenTime");
    field.setAccessible(true);
    field.set(tokenCacheManager, time);
  }

  @Nested
  @DisplayName("getToken()")
  class GetToken {

    @Test
    @DisplayName("returns cached token when it is still fresh")
    void returnsCachedTokenWhenFresh() throws Exception {
      // Pre-populate the cache with a fresh token
      setCachedToken("Bearer test-token-123");
      setCachedTokenTime(System.currentTimeMillis());

      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer test-token-123");
      // Should NOT have called the remote service
      verify(clientSignInnerApiRemote, times(0)).signin(any());
    }

    @Test
    @DisplayName("falls back to expired cached token when refresh fails")
    void fallsBackToExpiredCachedToken() throws Exception {
      // Set a cached token that is expired (cached long ago)
      setCachedToken("Bearer expired-token");
      setCachedTokenTime(0); // Very old timestamp

      // The getToken() method will try to refresh. buildTokenRequest() reads env vars
      // which are not set in test, causing SysException. The catch block returns
      // the expired cached token as fallback.
      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer expired-token");
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
    void doesNotCallRemoteForFreshToken() throws Exception {
      // Set token that was cached 5 minutes ago (within 13-min effective duration)
      setCachedToken("Bearer recent-token");
      setCachedTokenTime(System.currentTimeMillis() - Duration.ofMinutes(5).toMillis());

      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer recent-token");
      verify(clientSignInnerApiRemote, times(0)).signin(any());
    }

    @Test
    @DisplayName("attempts refresh when token age exceeds effective cache duration")
    void attemptsRefreshWhenExpired() throws Exception {
      // Set token that was cached 14 minutes ago (exceeds 13-min effective duration)
      setCachedToken("Bearer old-token");
      setCachedTokenTime(System.currentTimeMillis() - Duration.ofMinutes(14).toMillis());

      // Refresh will fail due to missing env vars, but falls back to cached token
      String token = tokenCacheManager.getToken();

      assertThat(token).isEqualTo("Bearer old-token");
    }
  }

  @Nested
  @DisplayName("getTokenWithRetry()")
  class GetTokenWithRetry {

    @Test
    @DisplayName("returns token on first successful attempt")
    void returnsTokenOnFirstAttempt() throws Exception {
      // Pre-populate cache with fresh token so getToken() returns immediately
      setCachedToken("Bearer cached-token");
      setCachedTokenTime(System.currentTimeMillis());

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
    @DisplayName("retries and returns expired cached token as fallback")
    void retriesAndReturnsFallback() throws Exception {
      // Set an expired cached token so getToken()'s fallback works
      setCachedToken("Bearer fallback-token");
      setCachedTokenTime(0);

      // First call to getToken() will try to refresh, fail (no env vars),
      // but return the expired cached token
      String token = tokenCacheManager.getTokenWithRetry();

      assertThat(token).isEqualTo("Bearer fallback-token");
    }

    @Test
    @DisplayName("succeeds without retrying when token is fresh")
    void succeedsWithoutRetryWhenFresh() throws Exception {
      setCachedToken("Bearer fresh-token");
      setCachedTokenTime(System.currentTimeMillis());

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
    @DisplayName("clears cached token and resets timestamp")
    void clearsCachedToken() throws Exception {
      setCachedToken("Bearer some-token");
      setCachedTokenTime(System.currentTimeMillis());

      assertThat(tokenCacheManager.hasCachedToken()).isTrue();

      tokenCacheManager.clearCache();

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
      assertThat(tokenCacheManager.getCachedTokenAge()).isEqualTo(-1);
    }

    @Test
    @DisplayName("clearCache is idempotent when already empty")
    void clearCacheIdempotent() {
      tokenCacheManager.clearCache();

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
      assertThat(tokenCacheManager.getCachedTokenAge()).isEqualTo(-1);
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
    void returnsTrueWhenFresh() throws Exception {
      setCachedToken("Bearer valid-token");
      setCachedTokenTime(System.currentTimeMillis());

      assertThat(tokenCacheManager.hasCachedToken()).isTrue();
    }

    @Test
    @DisplayName("returns false when cached token is expired")
    void returnsFalseWhenExpired() throws Exception {
      setCachedToken("Bearer expired-token");
      setCachedTokenTime(0); // Very old

      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }

    @Test
    @DisplayName("returns true when token is within effective cache duration")
    void returnsTrueWithinEffectiveDuration() throws Exception {
      setCachedToken("Bearer token");
      setCachedTokenTime(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());

      // Token age (10 min) is less than effective duration (13 min)
      assertThat(tokenCacheManager.hasCachedToken()).isTrue();
    }

    @Test
    @DisplayName("returns false when token exceeds effective cache duration")
    void returnsFalseExceedingEffectiveDuration() throws Exception {
      setCachedToken("Bearer token");
      setCachedTokenTime(System.currentTimeMillis() - Duration.ofMinutes(14).toMillis());

      // Token age (14 min) exceeds effective duration (13 min)
      assertThat(tokenCacheManager.hasCachedToken()).isFalse();
    }
  }

  @Nested
  @DisplayName("getCachedTokenAge()")
  class GetCachedTokenAge {

    @Test
    @DisplayName("returns -1 when no cached token")
    void returnsNegativeOneWhenNoCachedToken() {
      assertThat(tokenCacheManager.getCachedTokenAge()).isEqualTo(-1);
    }

    @Test
    @DisplayName("returns positive age when token is cached")
    void returnsPositiveAge() throws Exception {
      setCachedToken("Bearer token");
      setCachedTokenTime(System.currentTimeMillis() - 5000);

      long age = tokenCacheManager.getCachedTokenAge();
      assertThat(age).isGreaterThanOrEqualTo(4000).isLessThan(10000);
    }

    @Test
    @DisplayName("returns small age for recently cached token")
    void returnsSmallAgeForRecentToken() throws Exception {
      setCachedToken("Bearer token");
      setCachedTokenTime(System.currentTimeMillis());

      long age = tokenCacheManager.getCachedTokenAge();
      assertThat(age).isGreaterThanOrEqualTo(0).isLessThan(1000);
    }
  }

  @Nested
  @DisplayName("Thread Safety")
  class ThreadSafety {

    @Test
    @DisplayName("concurrent reads of cached token return consistent results")
    void concurrentReadsReturnConsistentResults() throws Exception {
      setCachedToken("Bearer concurrent-token");
      setCachedTokenTime(System.currentTimeMillis());

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
      setCachedToken("Bearer token-to-clear");
      setCachedTokenTime(System.currentTimeMillis());

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
