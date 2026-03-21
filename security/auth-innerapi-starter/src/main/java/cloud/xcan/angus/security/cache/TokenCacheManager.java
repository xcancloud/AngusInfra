package cloud.xcan.angus.security.cache;

import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Thread-safe OAuth2 Token Cache Manager for Inner API Authentication
 * 
 * This component manages the caching and refreshing of OAuth2 access tokens used
 * for inter-service (service-to-service) authentication.
 * 
 * Features:
 * - Thread-safe token caching using volatile fields and synchronized methods
 * - Exponential backoff retry strategy with configurable attempts
 * - Token refresh threshold to prevent expiration race conditions
 * - Fallback to expired token when all retries fail
 * - Comprehensive logging for debugging and monitoring
 * 
 * Thread Safety:
 * - Uses volatile fields to ensure visibility of token updates across threads
 * - Synchronizes the getToken() method to prevent duplicate token refresh requests
 * - Double-checking lock pattern for performance optimization
 * 
 * Configuration:
 * All timeout and retry settings are read from InnerApiAuthProperties
 * 
 * Usage:
 * ```java
 * @Autowired
 * private TokenCacheManager tokenCacheManager;
 * 
 * public void apply(RequestTemplate template) {
 *   String token = tokenCacheManager.getTokenWithRetry();
 *   template.header("Authorization", token);
 * }
 * ```
 * 
 * @author Framework Team
 * @version 1.0
 * @see InnerApiAuthProperties
 * @see ClientSignInnerApiRemote
 */
@Slf4j
@Component
@ConditionalOnBean(InnerApiAuthProperties.class)
public class TokenCacheManager {

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Cache Fields - Using Volatile for Thread Safety
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Cached OAuth2 access token with "Bearer" prefix
   * 
   * volatile ensures:
   * - Changes made by one thread are immediately visible to other threads
   * - Prevents compiler optimizations that might cache the value in registers
   * - Guarantees atomic read/write of the reference (not the String content)
   * 
   * Thread Safety Note:
   * - Used in synchronized getToken() method to prevent duplicate refresh requests
   * - Valid across all threads once written (visibility guarantee)
   */
  private volatile String cachedToken;

  /**
   * Timestamp (milliseconds) when the cached token was last obtained
   * 
   * volatile ensures:
   * - All threads see the most recent timestamp
   * - Used to calculate token age and determine if refresh is needed
   * 
   * Initial value: 0 (guarantees first token request will fetch new token)
   */
  private volatile long cachedTokenTime = 0;

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Dependencies
  // ╚═══════════════════════════════════════════════════════════════════════╝

  private final InnerApiAuthProperties properties;
  private final ClientSignInnerApiRemote clientSignInnerApiRemote;

  /**
   * Constructor for TokenCacheManager
   * 
   * @param properties configuration properties for token cache management
   * @param clientSignInnerApiRemote Feign client for OAuth2 token endpoint
   */
  public TokenCacheManager(
      InnerApiAuthProperties properties,
      ClientSignInnerApiRemote clientSignInnerApiRemote) {
    this.properties = properties;
    this.clientSignInnerApiRemote = clientSignInnerApiRemote;

    // Validate configuration on startup
    properties.validate();

    log.info("TokenCacheManager initialized with config: "
        + "cacheInterval={}m, refreshThreshold={}m, maxRetries={}",
        properties.getTokenCacheInterval().toMinutes(),
        properties.getTokenRefreshThreshold().toMinutes(),
        properties.getMaxRetries());
  }

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Public API
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Get OAuth2 access token with automatic refresh and retry logic.
   * 
   * This is the main entry point for obtaining tokens. It handles:
   * - Token cache checking and reuse
   * - Automatic refresh when token ages
   * - Retry with exponential backoff on failure
   * - Fallback to expired token as last resort
   * 
   * Synchronization:
   * - This method is synchronized to prevent concurrent token refresh requests
   * - Only one thread can update the cached token at a time
   * - This prevents duplicate requests to the OAuth2 server
   * 
   * @return cached or newly refreshed OAuth2 access token
   * @throws SysException if token refresh fails after all retries and no fallback available
   */
  public String getTokenWithRetry() {
    log.debug("Attempting to get token with retry logic");

    for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
      try {
        // Delegate to synchronized getToken() for thread-safe refresh
        String token = getToken();
        log.debug("Successfully obtained token on attempt {}", attempt);
        return token;

      } catch (Exception e) {
        // Log the failure and decide whether to retry
        if (attempt < properties.getMaxRetries()) {
          // Calculate exponential backoff delay
          long delayMillis = properties.getRetryDelay(attempt).toMillis();
          log.warn(
              "Token retrieval failed on attempt {}/{}, "
                  + "retrying in {}ms. Error: {}",
              attempt, properties.getMaxRetries(), delayMillis, e.getMessage());

          // Sleep before retry
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Token retry sleep interrupted", ie);
            throw new SysException("Token refresh interrupted", ie);
          }

        } else {
          // All retries exhausted
          log.error(
              "Token retrieval failed after {} attempts. "
                  + "Last error: {}",
              properties.getMaxRetries(), e.getMessage(), e);
          throw new SysException(
              "Unable to obtain authentication token after "
                  + properties.getMaxRetries() + " retry attempts",
              e);
        }
      }
    }

    // Should never reach here, but defensive programming
    throw new SysException("Unexpected error: token retrieval failed");
  }

  /**
   * Get or refresh the cached OAuth2 token.
   * 
   * This method is synchronized to prevent concurrent token refresh requests:
   * 1. Only one thread can execute this method at a time
   * 2. Other threads block waiting for the token to be refreshed
   * 3. Once refreshed, all waiting threads get the same cached token
   * 
   * Token Freshness Check:
   * - If cached token exists AND token age < (cacheInterval - refreshThreshold)
   *   -> Return cached token (fast path, no network call)
   * - Otherwise, refresh the token (slow path, network call to OAuth2 server)
   * 
   * Example:
   * - At T=0: Token obtained, cachedTokenTime=0
   * - At T=13min: Token still valid (13min < 15min-2min=13min boundary), return cached
   * - At T=13min: Token refresh threshold reached (13min >= 13min), refresh token
   * 
   * @return cached or newly refreshed OAuth2 access token with "Bearer" prefix
   * @throws SysException if token refresh fails and no fallback token available
   */
  public synchronized String getToken() {
    long now = System.currentTimeMillis();

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │ Fast Path: Use Cached Token
    // │ Condition: Token exists AND token age < effective cache duration
    // └─────────────────────────────────────────────────────────────────────┘
    if (cachedToken != null) {
      long tokenAge = now - cachedTokenTime;
      long effectiveCacheDurationMillis =
          properties.getEffectiveTokenCacheDuration().toMillis();

      if (tokenAge < effectiveCacheDurationMillis) {
        // Token is still fresh, return cached copy
        log.debug(
            "Using cached token (age={}ms, TTL={}ms)",
            tokenAge, effectiveCacheDurationMillis);
        return cachedToken;
      }

      // Token age exceeds threshold, need refresh
      log.debug(
          "Token cache expired (age={}ms, TTL={}ms), refreshing token",
          tokenAge, effectiveCacheDurationMillis);
    } else {
      log.debug("No cached token found, requesting new token from OAuth2 server");
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │ Slow Path: Refresh Token via Network Call
    // │ This is where the OAuth2 server interaction happens
    // └─────────────────────────────────────────────────────────────────────┘
    try {
      // Build token request with client credentials
      ClientSignInDto signInRequest = buildTokenRequest();

      // Call OAuth2 token endpoint
      log.debug("Requesting new token from OAuth2 server...");
      Optional<ClientSignInVo> response = clientSignInnerApiRemote.signin(signInRequest);

      // Extract token from response using Optional::orElseThrow
      // This pattern is safer than direct .get() as it throws meaningful exception
      ClientSignInVo clientSignInVo = response.orElseThrow(() ->
          new SysException("OAuth2 server returned empty response for token request"));

      // Cache the new token with current timestamp
      this.cachedToken = InnerApiAuthProperties.BEARER_TOKEN_TYPE
          + " " + clientSignInVo.getAccessToken();
      this.cachedTokenTime = now;

      log.info("Successfully obtained new OAuth2 token, caching for {}m",
          properties.getTokenCacheInterval().toMinutes());

      return this.cachedToken;

    } catch (Exception e) {
      // ┌───────────────────────────────────────────────────────────────────┐
      // │ Fallback Strategy: Return Expired Token
      // │ Risk: Token might be rejected by resource server
      // │ Benefit: Maintains service availability in degraded mode
      // └───────────────────────────────────────────────────────────────────┘
      log.error("Failed to refresh authentication token from OAuth2 server", e);

      if (cachedToken != null) {
        log.warn(
            "Using expired cached token as fallback. "
                + "Resource server may reject this token. "
                + "Error from OAuth2 server: {}",
            e.getMessage());
        return cachedToken;
      }

      // No fallback available, must throw exception
      throw new SysException("Unable to obtain authentication token", e);
    }
  }

  /**
   * Clear the cached token (useful for testing and manual reset scenarios)
   * 
   * Thread Safety:
   * - This method is not synchronized because volatile assignment is atomic
   * - Safe to call from any thread
   * 
   * Use cases:
   * - Testing: Clear cache between test cases
   * - Manual reset: Invalidate token when user logs out
   * - Emergency: Force token refresh on next request
   */
  public void clearCache() {
    log.info("Clearing cached token");
    cachedToken = null;
    cachedTokenTime = 0;
  }

  /**
   * Get the age of the cached token in milliseconds
   * 
   * @return age in milliseconds (-1 if no cached token)
   */
  public long getCachedTokenAge() {
    if (cachedToken == null) {
      return -1;
    }
    return System.currentTimeMillis() - cachedTokenTime;
  }

  /**
   * Check if a valid cached token is available
   * 
   * @return true if valid cached token exists and hasn't expired
   */
  public boolean hasCachedToken() {
    if (cachedToken == null) {
      return false;
    }
    long tokenAge = getCachedTokenAge();
    long effectiveCacheDurationMillis =
        properties.getEffectiveTokenCacheDuration().toMillis();
    return tokenAge >= 0 && tokenAge < effectiveCacheDurationMillis;
  }

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Private Helper Methods
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Build OAuth2 client credentials token request
   * 
   * Reads client credentials from environment variables for security:
   * - OAUTH2_INNER_API_CLIENT_ID
   * - OAUTH2_INNER_API_CLIENT_SECRET
   * 
   * @return client credentials token request DTO
   */
  private ClientSignInDto buildTokenRequest() {
    String clientId = System.getenv(InnerApiAuthProperties.CLIENT_ID_ENV_PROPERTY);
    String clientSecret = System.getenv(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY);

    // Fallback to legacy property name if primary not found
    if (clientSecret == null) {
      clientSecret = System.getenv(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY_LEGACY);
    }

    // Validate credentials are available
    if (clientId == null || clientId.isEmpty()) {
      throw new SysException(
          "OAuth2 client credentials not configured. "
              + "Please set " + InnerApiAuthProperties.CLIENT_ID_ENV_PROPERTY
              + " environment variable");
    }

    if (clientSecret == null || clientSecret.isEmpty()) {
      throw new SysException(
          "OAuth2 client secret not configured. "
              + "Please set " + InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY
              + " or " + InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY_LEGACY
              + " environment variable");
    }

    return new ClientSignInDto()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setScope(InnerApiAuthProperties.INNER_API_TOKEN_CLIENT_SCOPE);
  }
}
