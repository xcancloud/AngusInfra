package cloud.xcan.angus.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SIGN2P_TOKEN_CLIENT_SCOPE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;

import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Feign Request Interceptor for OpenAPI 2P (Private) Authentication
 *
 * This interceptor handles authentication for OpenAPI 2P requests with support for:
 * <ol>
 *   <li>Cloud Service Edition: Uses current user's authorization context</li>
 *   <li>Traditional Edition: Uses service-level OAuth2 tokens</li>
 * </ol>
 *
 * Request Flow:
 * <ol>
 *   <li>For cloud service edition: Forward the current user's authorization header</li>
 *   <li>For traditional edition: Inject OAuth2 Bearer token from service credentials</li>
 * </ol>
 *
 * Configuration:
 * <pre>
 * xcan:
 *   auth:
 *     openapi2p:
 *       enabled: true
 *       request-path-prefix: /openapi2p
 *       token-cache-interval: 15m
 * </pre>
 *
 * Environment Variables:
 * <ul>
 *   <li>OAUTH2_OPENAPI2P_CLIENT_ID: OAuth2 client ID for OpenAPI 2P</li>
 *   <li>OAUTH2_OPENAPI2P_CLIENT_SECRET: OAuth2 client secret for OpenAPI 2P</li>
 * </ul>
 *
 * @author Framework Team
 * @version 2.0 (Redesigned without Str0 obfuscation, with configuration externalization)
 * @since 2025-03-21
 * @see FeignInnerApiAuthInterceptor
 */
@Slf4j
public class FeignOpenapi2pAuthInterceptor implements RequestInterceptor {

  /**
   * Cached service-level OAuth2 token for OpenAPI 2P. Volatile ensures visibility across threads.
   */
  private volatile String cachedOpenapi2pToken;

  /**
   * Timestamp of the last token acquisition, used for cache expiration.
   */
  private volatile long cachedTokenTime = 0;

  /**
   * Feign client for OAuth2 token endpoint
   */
  private final ClientSignOpenapi2pRemote clientSignOpenapi2pRemote;

  /**
   * Configuration properties for OpenAPI 2P authentication
   */
  private final Openapi2pAuthProperties properties;

  /**
   * Spring environment for reading credentials from environment variables
   */
  private final ConfigurableEnvironment environment;

  /**
   * Constructor for FeignOpenapi2pAuthInterceptor
   *
   * @param clientSignOpenapi2pRemote Feign client for token endpoint
   * @param properties                configuration properties
   * @param environment               Spring environment for credential resolution
   */
  public FeignOpenapi2pAuthInterceptor(ClientSignOpenapi2pRemote clientSignOpenapi2pRemote,
      Openapi2pAuthProperties properties, ConfigurableEnvironment environment) {
    this.clientSignOpenapi2pRemote = clientSignOpenapi2pRemote;
    this.properties = properties;
    this.environment = environment;
    log.info("FeignOpenapi2pAuthInterceptor initialized with path prefix: {}",
        properties.getRequestPathPrefix());
  }

  /**
   * Apply authentication to OpenAPI 2P requests
   *
   * Request paths matching the configured prefix receive token injection based on edition:
   * <ul>
   *   <li>Cloud Service Edition: Forward user's authorization (from PrincipalContext)</li>
   *   <li>Traditional Edition: Inject service OAuth2 token</li>
   * </ul>
   *
   * @param template Feign request template to modify
   */
  @Override
  public void apply(RequestTemplate template) {
    // Check if this is an OpenAPI 2P request using configuration-driven path matching
    if (!properties.shouldIntercept(template.path())) {
      log.trace("Request path '{}' is not an OpenAPI 2P request", template.path());
      return;
    }

    log.debug("Intercepting OpenAPI 2P request to path: {}", template.path());

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │ Cloud Service Edition: Forward User's Authorization
    // └─────────────────────────────────────────────────────────────────────┘
    if (isCloudServiceEdition()) {
      String userAuthorization = getAuthorization();
      if (StringUtils.isNotEmpty(userAuthorization)) {
        log.debug("Cloud service edition: forwarding user authorization header");
        template.header(Header.AUTHORIZATION, userAuthorization);
        return;
      }
      log.debug("Cloud service edition but no user authorization found in context");
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │ Traditional Edition: Inject Service OAuth2 Token
    // └─────────────────────────────────────────────────────────────────────┘
    try {
      String serviceToken = getServiceToken();
      if (StringUtils.isNotEmpty(serviceToken)) {
        template.header(Header.AUTHORIZATION, serviceToken);
        log.debug("Service-level authorization header injected for OpenAPI 2P path: {}",
            template.path());
      }
    } catch (Exception e) {
      log.error(
          "Failed to obtain service token for OpenAPI 2P request to: {}. "
              + "Request will proceed without authorization. Error: {}",
          template.path(), e.getMessage(), e);
      // Note: We don't throw exception here to allow graceful degradation
      // The resource server will handle the 401 response
    }
  }

  /**
   * Get service-level OAuth2 token for OpenAPI 2P
   *
   * Implements thread-safe caching with time-based expiration. Token is obtained from the OAuth2
   * server and cached for the configured interval. On failure, falls back to expired cached token if
   * available.
   *
   * @return OAuth2 Bearer token or null if unavailable
   */
  private synchronized String getServiceToken() {
    long now = System.currentTimeMillis();

    // Return cached token if still valid
    if (cachedOpenapi2pToken != null
        && now - cachedTokenTime < properties.getTokenCacheInterval().toMillis()) {
      log.trace("Returning cached service token");
      return cachedOpenapi2pToken;
    }

    log.debug("Requesting new service token from OpenAPI 2P OAuth2 endpoint");

    try {
      String clientId = environment.getProperty(Openapi2pAuthProperties.CLIENT_ID_ENV_PROPERTY);
      String clientSecret = environment.getProperty(
          Openapi2pAuthProperties.CLIENT_SECRET_ENV_PROPERTY);

      ClientSignInDto tokenRequest = new ClientSignInDto()
          .setClientId(clientId)
          .setClientSecret(clientSecret)
          .setScope(SIGN2P_TOKEN_CLIENT_SCOPE);

      ClientSignInVo response = clientSignOpenapi2pRemote.signin(tokenRequest)
          .orElseContentThrow();

      cachedOpenapi2pToken = BEARER_TOKEN_TYPE + " " + response.getAccessToken();
      cachedTokenTime = now;

      log.info("Successfully obtained service token for OpenAPI 2P, caching for reuse");
      return cachedOpenapi2pToken;

    } catch (Exception e) {
      log.warn(
          "Failed to obtain service token for OpenAPI 2P authentication. Error: {}",
          e.getMessage(), e);

      // Fallback: return expired cached token if available
      if (cachedOpenapi2pToken != null) {
        log.warn("Using expired cached token as fallback for OpenAPI 2P");
        return cachedOpenapi2pToken;
      }

      throw new RuntimeException("OpenAPI 2P authentication failed", e);
    }
  }

  /**
   * Clear the cached service token
   *
   * Use this method when you want to force a new token to be obtained on the next request (e.g.,
   * after configuration changes).
   */
  public void clearCache() {
    log.info("Clearing cached OpenAPI 2P service token");
    cachedOpenapi2pToken = null;
    cachedTokenTime = 0;
  }
}
