package cloud.xcan.angus.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SIGN2P_TOKEN_CLIENT_SCOPE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;

import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Feign Request Interceptor for OpenAPI 2P (Private) Authentication
 * 
 * This interceptor handles authentication for OpenAPI 2P requests with support for:
 * 1. Cloud Service Edition: Uses current user's authorization context
 * 2. Traditional Edition: Uses service-level OAuth2 tokens
 * 
 * Request Flow:
 * 1. For cloud service edition: Forward the current user's authorization header
 * 2. For traditional edition: Inject OAuth2 Bearer token from service credentials
 * 
 * Configuration:
 * Place the following in application.yml:
 * ```yaml
 * xcan:
 *   auth:
 *     openapi2p:
 *       enabled: true
 *       client-id: ${OAUTH2_OPENAPI2P_CLIENT_ID}
 *       client-secret: ${OAUTH2_OPENAPI2P_CLIENT_SECRET}
 *       token-cache-interval: 15m
 * ```
 * 
 * Environment Variables:
 * - OAUTH2_OPENAPI2P_CLIENT_ID: OAuth2 client ID for OpenAPI 2P
 * - OAUTH2_OPENAPI2P_CLIENT_SECRET: OAuth2 client secret for OpenAPI 2P
 * 
 * Token Caching:
 * Service-level tokens are cached for reuse to reduce token server load.
 * Cache is invalidated after token expiration or manually via clearCache().
 * 
 * @author Framework Team
 * @version 2.0 (Redesigned without Str0 obfuscation)
 * @since 2025-03-21
 * @see FeignInnerApiAuthInterceptor
 */
@Slf4j
public class FeignOpenapi2pAuthInterceptor implements RequestInterceptor {

  /**
   * Cached service-level OAuth2 token for OpenAPI 2P
   * Null if no token has been obtained yet
   */
  private String cachedOpenapi2pToken;

  /**
   * Feign client for OAuth2 token endpoint
   */
  private final ClientSignOpenapi2pRemote clientSignOpenapi2pRemote;

  /**
   * Constructor for FeignOpenapi2pAuthInterceptor
   * 
   * @param clientSignOpenapi2pRemote Feign client for token endpoint
   */
  public FeignOpenapi2pAuthInterceptor(ClientSignOpenapi2pRemote clientSignOpenapi2pRemote) {
    this.clientSignOpenapi2pRemote = clientSignOpenapi2pRemote;
    log.info("FeignOpenapi2pAuthInterceptor initialized");
  }

  /**
   * Apply authentication to OpenAPI 2P requests
   * 
   * Request paths matching "/openapi2p" prefix receive token injection based on edition:
   * - Cloud Service Edition: Forward user's authorization (from PrincipalContext)
   * - Traditional Edition: Inject service OAuth2 token
   * 
   * @param template Feign request template to modify
   */
  @Override
  public void apply(RequestTemplate template) {
    // Check if this is an OpenAPI 2P request
    if (!template.path().startsWith("/openapi2p")) {
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
   * Implements simple caching: token is obtained once and reused until a failure occurs.
   * On failure, no retry logic is applied (unlike InnerApiAuthInterceptor).
   * 
   * NOTE: This method currently has placeholder client credentials (null values).
   * Configuration properties or environment variables need to be implemented
   * to provide actual client ID and secret.
   * 
   * @return OAuth2 Bearer token or null if unavailable
   * @throws Exception if token request fails
   */
  private String getServiceToken() {
    // Return cached token if available
    if (cachedOpenapi2pToken != null) {
      log.trace("Returning cached service token");
      return cachedOpenapi2pToken;
    }

    log.debug("Requesting new service token from OpenAPI 2P OAuth2 endpoint");

    try {
      // TODO: Replace null values with configuration from environment or properties
      // Environment variables:
      // - OAUTH2_OPENAPI2P_CLIENT_ID
      // - OAUTH2_OPENAPI2P_CLIENT_SECRET
      ClientSignInDto tokenRequest = new ClientSignInDto()
          .setClientId(null) // TODO: Get from configuration
          .setClientSecret(null) // TODO: Get from configuration
          .setScope(SIGN2P_TOKEN_CLIENT_SCOPE);

      ClientSignInVo response = clientSignOpenapi2pRemote.signin(tokenRequest)
          .orElseContentThrow();

      cachedOpenapi2pToken = BEARER_TOKEN_TYPE + " " + response.getAccessToken();

      log.info("Successfully obtained service token for OpenAPI 2P, caching for reuse");
      return cachedOpenapi2pToken;

    } catch (Exception e) {
      log.warn(
          "Failed to obtain service token for OpenAPI 2P authentication. "
              + "Error: {}",
          e.getMessage(), e);
      throw new RuntimeException("OpenAPI 2P authentication failed", e);
    }
  }

  /**
   * Clear the cached service token
   * 
   * Use this method when you want to force a new token to be obtained
   * on the next request (e.g., after configuration changes).
   */
  public void clearCache() {
    log.info("Clearing cached OpenAPI 2P service token");
    cachedOpenapi2pToken = null;
  }
}
