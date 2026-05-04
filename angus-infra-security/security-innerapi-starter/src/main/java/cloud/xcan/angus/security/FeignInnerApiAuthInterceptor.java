package cloud.xcan.angus.security;

import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTHORIZATION;

import cloud.xcan.angus.security.cache.TokenCacheManager;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign Request Interceptor for Inner API Authentication
 * <p>
 * This interceptor automatically injects OAuth2 Bearer tokens into Feign requests that target inner
 * API endpoints (service-to-service communication).
 * <p>
 * Features: - Automatic token injection for configured request paths - Thread-safe token caching
 * and refresh - Exponential backoff retry on failures - Configuration-driven path and timeout
 * management - Comprehensive logging for monitoring
 * <p>
 * Configuration: Place the following in application.yml: ```yaml xcan: auth: innerapi: enabled:
 * true request-path-prefixes: - /innerapi - /system-api token-cache-interval: 15m
 * token-refresh-threshold: 2m max-retries: 3 retry-interval: 1s connection-timeout: 5s
 * read-timeout: 10s ```
 * <p>
 * Environment Variables: - OAUTH2_INNER_API_CLIENT_ID: OAuth2 client ID for inner API -
 * OAUTH2_INNER_API_CLIENT_SECRET: OAuth2 client secret for inner API
 * <p>
 * Usage: Spring automatically registers this interceptor with Feign clients if
 * InnerApiAuthProperties is properly configured.
 *
 * @author Framework Team
 * @version 2.0 (Redesigned with TokenCacheManager)
 * @see TokenCacheManager
 * @see InnerApiAuthProperties
 * @since 2025-03-21
 */
@Slf4j
public class FeignInnerApiAuthInterceptor implements RequestInterceptor {

  private final TokenCacheManager tokenCacheManager;
  private final InnerApiAuthProperties properties;

  /**
   * Constructor for FeignInnerApiAuthInterceptor
   *
   * @param tokenCacheManager manages OAuth2 token caching and refresh
   * @param properties        configuration properties for path matching
   */
  public FeignInnerApiAuthInterceptor(
      TokenCacheManager tokenCacheManager,
      InnerApiAuthProperties properties) {
    this.tokenCacheManager = tokenCacheManager;
    this.properties = properties;

    log.info("FeignInnerApiAuthInterceptor initialized");
  }

  /**
   * Apply the interceptor to the Feign request template.
   * <p>
   * This method is called by Feign for every outgoing request. It decides whether to inject the
   * Authorization header based on the request path and configured prefixes.
   * <p>
   * Process: 1. Check if inner API authentication is enabled 2. Check if request path matches
   * configured prefixes 3. If matched, fetch token from cache manager (with retry logic) 4. Inject
   * token as "Authorization: Bearer <token>" header
   * <p>
   * Error Handling: - If token retrieval fails, the exception is propagated - The calling code can
   * handle this appropriately (e.g., circuit breaker) - No silent failures to ensure visibility of
   * authentication issues
   *
   * @param template Feign request template to modify
   * @throws RuntimeException if token retrieval fails (from tokenCacheManager)
   */
  @Override
  public void apply(RequestTemplate template) {
    // Quick check: Is feature enabled?
    if (!properties.isEnabled()) {
      log.trace("Inner API authentication is disabled");
      return;
    }

    // Quick check: Should we intercept this request?
    if (!properties.shouldIntercept(template.path())) {
      log.trace("Request path '{}' does not match any configured prefixes", template.path());
      return;
    }

    // Path matches configured prefix, inject authorization header
    log.debug("Intercepting request to path: {}", template.path());

    try {
      // Get token with automatic refresh and retry logic
      String token = tokenCacheManager.getTokenWithRetry();

      // Inject Authorization header
      template.header(AUTHORIZATION, token);

      log.debug("Authorization header injected for path: {}", template.path());

    } catch (Exception e) {
      // Token retrieval failed despite retries
      // Propagate exception to fail fast and provide visibility
      log.error(
          "Failed to obtain authentication token for inner API request to: {}. "
              + "Request will fail. Error: {}",
          template.path(), e.getMessage(), e);
      throw new RuntimeException(
          "Inner API authentication failed for path: " + template.path(), e);
    }
  }
}
