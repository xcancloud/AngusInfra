package cloud.xcan.angus.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SIGN2P_TOKEN_CLIENT_SCOPE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;

import cloud.xcan.angus.security.config.Openapi2pAuthProperties;
import cloud.xcan.angus.security.model.cache.TokenStore;
import cloud.xcan.angus.security.model.remote.dto.ClientSignInDto;
import cloud.xcan.angus.security.model.remote.vo.ClientSignInVo;
import cloud.xcan.angus.security.remote.ClientSignOpenapi2pRemote;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Feign Request Interceptor for OpenAPI 2P (Private) Authentication
 *
 * <p>Supports both local and distributed token caching via the {@link TokenStore} abstraction.</p>
 *
 * <p>Configuration:</p>
 * <pre>
 * xcan:
 *   auth:
 *     openapi2p:
 *       enabled: true
 *       cache-type: local        # or "distributed" for multi-instance
 *       cache-key: auth:openapi2p:token
 *       token-cache-interval: 15m
 * </pre>
 *
 * @author Framework Team
 * @version 3.0 (Refactored with TokenStore abstraction)
 * @see TokenStore
 * @since 2025-03-22
 */
@Slf4j
public class FeignOpenapi2pAuthInterceptor implements RequestInterceptor {

  private final ClientSignOpenapi2pRemote clientSignOpenapi2pRemote;
  private final Openapi2pAuthProperties properties;
  private final ConfigurableEnvironment environment;
  private final TokenStore tokenStore;

  /**
   * Fallback token kept in memory for degraded mode.
   */
  private volatile String fallbackToken;

  public FeignOpenapi2pAuthInterceptor(ClientSignOpenapi2pRemote clientSignOpenapi2pRemote,
      Openapi2pAuthProperties properties, ConfigurableEnvironment environment,
      TokenStore tokenStore) {
    this.clientSignOpenapi2pRemote = clientSignOpenapi2pRemote;
    this.properties = properties;
    this.environment = environment;
    this.tokenStore = tokenStore;
    log.info("FeignOpenapi2pAuthInterceptor initialized with path prefix: {}, cacheType: {}",
        properties.getRequestPathPrefix(), properties.getCacheType());
  }

  @Override
  public void apply(RequestTemplate template) {
    if (!properties.shouldIntercept(template.path())) {
      log.trace("Request path '{}' is not an OpenAPI 2P request", template.path());
      return;
    }

    log.debug("Intercepting OpenAPI 2P request to path: {}", template.path());

    if (isCloudServiceEdition()) {
      String userAuthorization = getAuthorization();
      if (StringUtils.isNotEmpty(userAuthorization)) {
        log.debug("Cloud service edition: forwarding user authorization header");
        template.header(Header.AUTHORIZATION, userAuthorization);
        return;
      }
      log.debug("Cloud service edition but no user authorization found in context");
    }

    try {
      String serviceToken = getServiceToken();
      if (StringUtils.isNotEmpty(serviceToken)) {
        template.header(Header.AUTHORIZATION, serviceToken);
        log.debug("Service-level authorization header injected for OpenAPI 2P path: {}",
            template.path());
      }
    } catch (Exception e) {
      log.error("Failed to obtain service token for OpenAPI 2P request to: {}. Error: {}",
          template.path(), e.getMessage(), e);
    }
  }

  private synchronized String getServiceToken() {
    // Fast path: check cache
    Optional<String> cached = tokenStore.retrieve(properties.getCacheKey());
    if (cached.isPresent()) {
      log.trace("Returning cached service token");
      return cached.get();
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

      String token = BEARER_TOKEN_TYPE + " " + response.getAccessToken();

      long ttlSeconds = properties.getTokenCacheInterval().getSeconds();
      tokenStore.store(properties.getCacheKey(), token, ttlSeconds);
      this.fallbackToken = token;

      log.info("Successfully obtained service token for OpenAPI 2P, caching for reuse");
      return token;

    } catch (Exception e) {
      log.warn("Failed to obtain service token for OpenAPI 2P. Error: {}",
          e.getMessage(), e);

      if (fallbackToken != null) {
        log.warn("Using fallback cached token for OpenAPI 2P");
        return fallbackToken;
      }

      throw new RuntimeException("OpenAPI 2P authentication failed", e);
    }
  }

  public void clearCache() {
    log.info("Clearing cached OpenAPI 2P service token");
    tokenStore.remove(properties.getCacheKey());
    fallbackToken = null;
  }
}
