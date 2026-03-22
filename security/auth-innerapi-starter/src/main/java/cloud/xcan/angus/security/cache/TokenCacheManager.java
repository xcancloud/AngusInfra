package cloud.xcan.angus.security.cache;

import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import cloud.xcan.angus.security.model.cache.TokenStore;
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
 * <p>Manages the caching and refreshing of OAuth2 access tokens used for inter-service
 * (service-to-service) authentication. Supports both local (in-memory) and distributed cache
 * backends via the {@link TokenStore} abstraction.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Pluggable cache backend via {@link TokenStore} (local or distributed)</li>
 *   <li>Thread-safe token refresh via {@code synchronized} methods</li>
 *   <li>Exponential backoff retry strategy with configurable attempts</li>
 *   <li>Token refresh threshold to prevent expiration race conditions</li>
 *   <li>Fallback to expired token when all retries fail</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <pre>
 * xcan:
 *   auth:
 *     innerapi:
 *       cache-type: local        # or "distributed" for multi-instance
 *       cache-key: auth:innerapi:token
 *       token-cache-interval: 15m
 * </pre>
 *
 * @author Framework Team
 * @version 2.0 (Refactored with TokenStore abstraction)
 * @see TokenStore
 * @see InnerApiAuthProperties
 */
@Slf4j
@Component
@ConditionalOnBean(InnerApiAuthProperties.class)
public class TokenCacheManager {

  private final InnerApiAuthProperties properties;
  private final ClientSignInnerApiRemote clientSignInnerApiRemote;
  private final TokenStore tokenStore;

  /**
   * Fallback token kept in memory for degraded mode. Used when token refresh fails but a previous
   * token was obtained.
   */
  private volatile String fallbackToken;

  /**
   * Constructor for TokenCacheManager
   *
   * @param properties               configuration properties
   * @param clientSignInnerApiRemote Feign client for OAuth2 token endpoint
   * @param tokenStore               pluggable cache backend
   */
  public TokenCacheManager(
      InnerApiAuthProperties properties,
      ClientSignInnerApiRemote clientSignInnerApiRemote,
      TokenStore tokenStore) {
    this.properties = properties;
    this.clientSignInnerApiRemote = clientSignInnerApiRemote;
    this.tokenStore = tokenStore;

    // Validate configuration on startup
    properties.validate();

    log.info("TokenCacheManager initialized with config: "
            + "cacheType={}, cacheInterval={}m, refreshThreshold={}m, maxRetries={}",
        properties.getCacheType(),
        properties.getTokenCacheInterval().toMinutes(),
        properties.getTokenRefreshThreshold().toMinutes(),
        properties.getMaxRetries());
  }

  /**
   * Get OAuth2 access token with automatic refresh and retry logic.
   *
   * @return cached or newly refreshed OAuth2 access token
   * @throws SysException if token refresh fails after all retries and no fallback available
   */
  public String getTokenWithRetry() {
    log.debug("Attempting to get token with retry logic");

    for (int attempt = 1; attempt <= properties.getMaxRetries(); attempt++) {
      try {
        String token = getToken();
        log.debug("Successfully obtained token on attempt {}", attempt);
        return token;

      } catch (Exception e) {
        if (attempt < properties.getMaxRetries()) {
          long delayMillis = properties.getRetryDelay(attempt).toMillis();
          log.warn(
              "Token retrieval failed on attempt {}/{}, retrying in {}ms. Error: {}",
              attempt, properties.getMaxRetries(), delayMillis, e.getMessage());

          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Token retry sleep interrupted", ie);
            throw new SysException("Token refresh interrupted", ie);
          }

        } else {
          log.error("Token retrieval failed after {} attempts. Last error: {}",
              properties.getMaxRetries(), e.getMessage(), e);
          throw new SysException(
              "Unable to obtain authentication token after "
                  + properties.getMaxRetries() + " retry attempts",
              e);
        }
      }
    }

    throw new SysException("Unexpected error: token retrieval failed");
  }

  /**
   * Get or refresh the cached OAuth2 token.
   *
   * @return cached or newly refreshed OAuth2 access token with "Bearer" prefix
   * @throws SysException if token refresh fails and no fallback token available
   */
  public synchronized String getToken() {
    // Fast Path: Check token store
    Optional<String> cached = tokenStore.retrieve(properties.getCacheKey());
    if (cached.isPresent()) {
      log.debug("Using cached token from store");
      return cached.get();
    }

    log.debug("No valid cached token, requesting new token from OAuth2 server");

    // Slow Path: Refresh token via network call
    try {
      ClientSignInDto signInRequest = buildTokenRequest();

      log.debug("Requesting new token from OAuth2 server...");
      ClientSignInVo clientSignInVo = clientSignInnerApiRemote.signin(signInRequest)
          .orElseContentThrow();

      String token = InnerApiAuthProperties.BEARER_TOKEN_TYPE
          + " " + clientSignInVo.getAccessToken();

      // Store in cache backend with effective TTL
      long ttlSeconds = properties.getEffectiveTokenCacheDuration().getSeconds();
      tokenStore.store(properties.getCacheKey(), token, ttlSeconds);

      // Keep fallback copy
      this.fallbackToken = token;

      log.info("Successfully obtained new OAuth2 token, caching for {}m",
          properties.getTokenCacheInterval().toMinutes());

      return token;

    } catch (Exception e) {
      log.error("Failed to refresh authentication token from OAuth2 server", e);

      if (fallbackToken != null) {
        log.warn("Using fallback token. Resource server may reject this token. "
            + "Error from OAuth2 server: {}", e.getMessage());
        return fallbackToken;
      }

      throw new SysException("Unable to obtain authentication token", e);
    }
  }

  /**
   * Clear the cached token.
   */
  public void clearCache() {
    log.info("Clearing cached token");
    tokenStore.remove(properties.getCacheKey());
    fallbackToken = null;
  }

  /**
   * Check if a valid cached token is available.
   *
   * @return true if valid cached token exists
   */
  public boolean hasCachedToken() {
    return tokenStore.exists(properties.getCacheKey());
  }

  /**
   * Build OAuth2 client credentials token request.
   */
  private ClientSignInDto buildTokenRequest() {
    String clientId = System.getenv(InnerApiAuthProperties.CLIENT_ID_ENV_PROPERTY);
    String clientSecret = System.getenv(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY);

    if (clientSecret == null) {
      clientSecret = System.getenv(InnerApiAuthProperties.CLIENT_SECRET_ENV_PROPERTY_LEGACY);
    }

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
