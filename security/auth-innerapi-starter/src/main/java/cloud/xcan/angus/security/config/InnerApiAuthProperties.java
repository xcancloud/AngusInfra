package cloud.xcan.angus.security.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Inner API Authentication Configuration Properties
 * 
 * This configuration class provides externalized configuration for the Feign interceptor
 * used for inter-service authentication with the OAuth2 token server.
 * 
 * Configuration properties:
 * ```yaml
 * xcan:
 *   auth:
 *     innerapi:
 *       enabled: true
 *       request-path-prefixes:
 *         - /innerapi
 *         - /system-api
 *       token-cache-interval: 15m
 *       token-refresh-threshold: 2m
 *       max-retries: 3
 *       retry-interval: 1s
 *       connection-timeout: 5s
 *       read-timeout: 10s
 * ```
 * 
 * @author Framework Team
 * @version 1.0
 * @since 2025-03-21
 */
@Component
@ConfigurationProperties(prefix = "xcan.auth.innerapi")
@Data
public class InnerApiAuthProperties {

  /**
   * Enable or disable inner API authentication interceptor
   * Default: true (enabled)
   */
  private boolean enabled = true;

  /**
   * Request path prefixes that should be intercepted for token injection.
   * If a request path starts with any of these prefixes, the Authorization header will be added.
   * 
   * Default: ["/innerapi"]
   * 
   * Examples:
   * - /innerapi        - Feign internal API path
   * - /system-api      - System reserved API path
   * - /admin-api       - Admin only API path
   */
  private List<String> requestPathPrefixes = new ArrayList<String>() {
    {
      add("/innerapi");
    }
  };

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Token Cache Configuration
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Token cache validity period.
   * Once the token is obtained, it will be cached and reused for this duration.
   * When this period expires, a new token will be requested.
   * 
   * Default: 15 minutes
   * Note: The registered client token validity should not be less than this duration
   * 
   * IMPORTANT: The OAuth2 server's registered client token cannot be less than 15 minutes.
   * This property should match or be less than the OAuth2 server's token TTL.
   * 
   * @see #tokenRefreshThreshold
   */
  private Duration tokenCacheInterval = Duration.ofMinutes(15);

  /**
   * Token refresh threshold - how long before expiration to refresh.
   * If the token will expire within this threshold, it will be refreshed proactively.
   * 
   * Example: If tokenCacheInterval=15m and tokenRefreshThreshold=2m,
   * the token will be refreshed after 13 minutes (15-2) of the cache interval.
   * 
   * Default: 2 minutes
   * 
   * This helps avoid race conditions where the token might expire between
   * the check and the actual request.
   * 
   * @see #tokenCacheInterval
   */
  private Duration tokenRefreshThreshold = Duration.ofMinutes(2);

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Retry Configuration
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Maximum number of retry attempts when token refresh fails.
   * 
   * Retry strategy:
   * - Attempt 1: Immediate
   * - Attempt 2: Wait 1 * retryInterval (exponential backoff)
   * - Attempt 3: Wait 2 * retryInterval (exponential backoff)
   * - ... and so on
   * 
   * Default: 3 retries
   * 
   * If all retries fail, the interceptor will:
   * 1. Return the expired cached token (if available) as fallback
   * 2. Throw SysException if no cached token exists
   * 
   * @see #retryInterval
   */
  private int maxRetries = 3;

  /**
   * Delay between retry attempts (using exponential backoff).
   * 
   * Example with retryInterval=1s:
   * - Retry 1: Fail -> Wait 1s
   * - Retry 2: Fail -> Wait 2s
   * - Retry 3: Fail -> Wait 3s
   * 
   * Calculation: delay = baseRetryInterval * attemptNumber
   * 
   * Default: 1 second
   * 
   * @see #maxRetries
   */
  private Duration retryInterval = Duration.ofSeconds(1);

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ HTTP Client Configuration
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Connection timeout for token endpoint requests.
   * Duration to wait for establishing a TCP connection to the OAuth2 server.
   * 
   * Default: 5 seconds
   * 
   * If the connection cannot be established within this time,
   * a connection timeout exception will be thrown and retry logic will be triggered.
   */
  private Duration connectionTimeout = Duration.ofSeconds(5);

  /**
   * Read timeout for token endpoint requests.
   * Duration to wait for reading the response from the OAuth2 server.
   * 
   * Default: 10 seconds
   * 
   * If the response is not received within this time,
   * a read timeout exception will be thrown and retry logic will be triggered.
   */
  private Duration readTimeout = Duration.ofSeconds(10);

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Client Authentication Configuration
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * OAuth2 inner API client ID.
   * This client is used for service-to-service authentication.
   * 
   * IMPORTANT: This should be provided via environment variables or Spring configuration
   * to keep credentials secure. Do NOT hardcode in application.yml.
   * 
   * Example environment variable: OAUTH2_INNER_API_CLIENT_ID=inner-api-service
   */
  public static final String CLIENT_ID_ENV_PROPERTY = "OAUTH2_INNER_API_CLIENT_ID";

  /**
   * OAuth2 inner API client secret.
   * This secret is used for authenticating the inner API client.
   * 
   * IMPORTANT: This should be provided via environment variables in production.
   * NEVER hardcode this in configuration files.
   * 
   * Example environment variable: OAUTH2_INNER_API_CLIENT_SECRET=secret-key-here
   * 
   * Fallback properties for discovery:
   * 1. OAUTH2_INNER_API_CLIENT_SECRET (preferred)
   * 2. INNER_API_CLIENT_SECRET (legacy)
   */
  public static final String CLIENT_SECRET_ENV_PROPERTY = "OAUTH2_INNER_API_CLIENT_SECRET";
  public static final String CLIENT_SECRET_ENV_PROPERTY_LEGACY = "INNER_API_CLIENT_SECRET";

  /**
   * OAuth2 scopes for inner API token requests.
   * Typically includes necessary permissions for inter-service communication.
   * 
   * Example: "openid profile email"
   */
  public static final String INNER_API_TOKEN_CLIENT_SCOPE = "innerapi";

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Constants
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Default inner API request path prefix
   */
  public static final String DEFAULT_INNER_API_PATH_PREFIX = "/innerapi";

  /**
   * Bearer token type (OAuth2 standard)
   */
  public static final String BEARER_TOKEN_TYPE = "Bearer";

  /**
   * Token endpoint relative path
   */
  public static final String TOKEN_ENDPOINT = "/oauth2/token";

  /**
   * Grant type for client credentials flow
   */
  public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  // ╔═══════════════════════════════════════════════════════════════════════╗
  // ║ Validation Methods
  // ╚═══════════════════════════════════════════════════════════════════════╝

  /**
   * Validate the configuration properties
   * 
   * @throws IllegalArgumentException if configuration is invalid
   */
  public void validate() {
    if (tokenCacheInterval.toMinutes() < 1) {
      throw new IllegalArgumentException(
          "tokenCacheInterval must be at least 1 minute, got: " + tokenCacheInterval);
    }

    if (tokenRefreshThreshold.compareTo(tokenCacheInterval) >= 0) {
      throw new IllegalArgumentException(
          "tokenRefreshThreshold must be less than tokenCacheInterval. "
              + "refreshThreshold=" + tokenRefreshThreshold
              + ", cacheInterval=" + tokenCacheInterval);
    }

    if (maxRetries < 0) {
      throw new IllegalArgumentException(
          "maxRetries must be >= 0, got: " + maxRetries);
    }

    if (retryInterval.toMillis() < 100) {
      throw new IllegalArgumentException(
          "retryInterval must be at least 100ms, got: " + retryInterval);
    }

    if (connectionTimeout.toMillis() < 500) {
      throw new IllegalArgumentException(
          "connectionTimeout must be at least 500ms, got: " + connectionTimeout);
    }

    if (readTimeout.toMillis() < 1000) {
      throw new IllegalArgumentException(
          "readTimeout must be at least 1000ms, got: " + readTimeout);
    }

    if (requestPathPrefixes == null || requestPathPrefixes.isEmpty()) {
      throw new IllegalArgumentException(
          "requestPathPrefixes must not be empty");
    }
  }

  /**
   * Check if the given path should be intercepted for token injection
   * 
   * @param path the request path
   * @return true if the path starts with any configured prefix
   */
  public boolean shouldIntercept(String path) {
    if (path == null || !enabled) {
      return false;
    }

    return requestPathPrefixes.stream()
        .anyMatch(path::startsWith);
  }

  /**
   * Get the effective token cache validity period considering refresh threshold
   * 
   * @return the period after which a cached token should be refreshed
   */
  public Duration getEffectiveTokenCacheDuration() {
    return tokenCacheInterval.minus(tokenRefreshThreshold);
  }

  /**
   * Calculate exponential backoff delay for a given retry attempt
   * 
   * @param attemptNumber the current retry attempt number (1-based)
   * @return the duration to wait before the next retry
   */
  public Duration getRetryDelay(int attemptNumber) {
    return retryInterval.multipliedBy(attemptNumber);
  }
}
