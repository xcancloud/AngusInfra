package cloud.xcan.angus.security.authentication;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_EXPIRED;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_LOCKED;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_BIZ_TAG;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_ID_ISSUED_AT;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_SECRET_EXPIRES_AT;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_COUNTRY;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CREDENTIALS_NON_EXPIRED;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DESCRIPTION;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DIRECTORY_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_EMAIL;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ENABLED;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_EXPIRED_DATE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_FIRST_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_FULL_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_GRANT_TYPE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_IS_USER_TOKEN;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_LAST_MODIFIED_PASSWORD_DATE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_LAST_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_MOBILE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PASSWORD_EXPIRED_DATE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PERMISSION;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PLATFORM;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PRINCIPAL;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ROOT_REQUEST_id;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_SCOPES;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_SYS_ADMIN;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_REAL_NAME_STATUS;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TO_USER;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_USERNAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.CUSTOM_ACCESS_TOKEN_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTH_DEVICE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.DEVICE_ID_IN_QUERY;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.REMOTE_ADDR_IN_QUERY;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.USER_AGENT;
import static cloud.xcan.angus.spec.http.HttpRequestHeader.User_Agent;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getRequestStringAttribute;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.core.converter.ClaimConversionService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public final class CustomOAuth2TokenIntrospectionAuthenticationProvider implements
    AuthenticationProvider {

  private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);

  private static final TypeDescriptor LIST_STRING_TYPE_DESCRIPTOR = TypeDescriptor.collection(
      List.class, TypeDescriptor.valueOf(String.class));

  private final RegisteredClientRepository registeredClientRepository;

  private final OAuth2AuthorizationService authorizationService;

  /**
   * Constructs an {@code CustomOAuth2TokenIntrospectionAuthenticationProvider} using the provided
   * parameters.
   *
   * @param registeredClientRepository the repository of registered clients
   * @param authorizationService       the authorization service
   */
  public CustomOAuth2TokenIntrospectionAuthenticationProvider(
      RegisteredClientRepository registeredClientRepository,
      OAuth2AuthorizationService authorizationService) {
    Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
    Assert.notNull(authorizationService, "authorizationService cannot be null");
    this.registeredClientRepository = registeredClientRepository;
    this.authorizationService = authorizationService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication
        = (OAuth2TokenIntrospectionAuthenticationToken) authentication;

    OAuth2ClientAuthenticationToken clientPrincipal
        = getAuthenticatedClientElseThrowInvalidClient(tokenIntrospectionAuthentication);

    OAuth2Authorization authorization = this.authorizationService
        .findByToken(tokenIntrospectionAuthentication.getToken(), null);
    if (authorization == null) {
      if (log.isTraceEnabled()) {
        log.trace("Did not authenticate token introspection request since token was not found");
      }
      // Return the authentication request when token not found
      return tokenIntrospectionAuthentication;
    }

    if (log.isTraceEnabled()) {
      log.trace("Retrieved authorization with token");
    }

    OAuth2Authorization.Token<OAuth2Token> authorizedToken = authorization
        .getToken(tokenIntrospectionAuthentication.getToken());
    assert authorizedToken != null;
    if (!authorizedToken.isActive()) {
      if (log.isTraceEnabled()) {
        log.trace("Did not introspect token since not active");
      }
      return new OAuth2TokenIntrospectionAuthenticationToken(
          tokenIntrospectionAuthentication.getToken(),
          clientPrincipal, OAuth2TokenIntrospection.builder().build());
    }

    RegisteredClient authorizedClient = this.registeredClientRepository
        .findByClientId(authorization.getRegisteredClientId());
    OAuth2TokenIntrospection tokenClaims = withActiveTokenClaims(authorization,
        authorizedToken, authorizedClient);

    if (log.isTraceEnabled()) {
      log.trace("Authenticated token introspection request");
    }

    return new OAuth2TokenIntrospectionAuthenticationToken(
        authorizedToken.getToken().getTokenValue(), clientPrincipal, tokenClaims);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return OAuth2TokenIntrospectionAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private static OAuth2TokenIntrospection withActiveTokenClaims(
      OAuth2Authorization authorization, OAuth2Authorization.Token<OAuth2Token> authorizedToken,
      RegisteredClient authorizedClient) {

    OAuth2TokenIntrospection.Builder tokenClaims;
    if (!CollectionUtils.isEmpty(authorizedToken.getClaims())) {
      Map<String, Object> claims = convertClaimsIfNecessary(authorizedToken.getClaims());
      tokenClaims = OAuth2TokenIntrospection.withClaims(claims).active(true);
    } else {
      tokenClaims = OAuth2TokenIntrospection.builder(true);
    }

    tokenClaims.clientId(authorizedClient.getClientId());

    OAuth2Token token = authorizedToken.getToken();
    if (token.getIssuedAt() != null) {
      tokenClaims.issuedAt(token.getIssuedAt());
    }
    if (token.getExpiresAt() != null) {
      tokenClaims.expiresAt(token.getExpiresAt());
    }

    if (OAuth2AccessToken.class.isAssignableFrom(token.getClass())) {
      OAuth2AccessToken accessToken = (OAuth2AccessToken) token;
      tokenClaims.tokenType(accessToken.getTokenType().getValue());
    }

    AuthorizationGrantType grantType = authorization.getAuthorizationGrantType();
    tokenClaims.claim(INTROSPECTION_CLAIM_NAMES_GRANT_TYPE, grantType.getValue());

    if (grantType.equals(AuthorizationGrantType.PASSWORD)) {
      Object principal = authorization.getAttribute(Principal.class.getName());
      if (principal != null) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
          CustomOAuth2User user = (CustomOAuth2User)
              ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
          tokenClaims.claim(INTROSPECTION_CLAIM_NAMES_PRINCIPAL,
              toUserPrincipalClaim(user, authorization.getAttributes()));
          if (isNotEmpty(user.getAuthorities())) {
            tokenClaims.claim(INTROSPECTION_CLAIM_NAMES_PERMISSION, user.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
          }
        }
      }
    } else if (grantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
      tokenClaims.claim(INTROSPECTION_CLAIM_NAMES_PRINCIPAL, toClientPrincipalClaim(
          (CustomOAuth2RegisteredClient) authorizedClient));
    }
    return tokenClaims.build();
  }

  private static Map<String, Object> toUserPrincipalClaim(CustomOAuth2User user,
      Map<String, Object> attributes) {
    String userTokenName = nonNull(attributes) && attributes.containsKey(CUSTOM_ACCESS_TOKEN_NAME)
        ? attributes.get(CUSTOM_ACCESS_TOKEN_NAME).toString() : null;

    Map<String, Object> claims = new HashMap<>();
    claims.put(INTROSPECTION_CLAIM_NAMES_USERNAME, user.getUsername());
    claims.put(INTROSPECTION_CLAIM_NAMES_ENABLED, user.isEnabled());
    claims.put(INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_EXPIRED, user.isAccountNonExpired());
    claims.put(INTROSPECTION_CLAIM_NAMES_ACCOUNT_NON_LOCKED, user.isAccountNonLocked());
    claims.put(INTROSPECTION_CLAIM_NAMES_CREDENTIALS_NON_EXPIRED, user.isCredentialsNonExpired());
    claims.put(INTROSPECTION_CLAIM_NAMES_ID, user.getId());
    claims.put(INTROSPECTION_CLAIM_NAMES_FIRST_NAME, user.getFirstName());
    claims.put(INTROSPECTION_CLAIM_NAMES_LAST_NAME, user.getLastName());
    claims.put(INTROSPECTION_CLAIM_NAMES_FULL_NAME, nullSafe(userTokenName, user.getFullName()));
    //claims.put(INTROSPECTION_CLAIM_NAMES_PASSWORD_STRENGTH, user.getPasswordStrength());
    claims.put(INTROSPECTION_CLAIM_NAMES_SYS_ADMIN, user.isSysAdmin());
    claims.put(INTROSPECTION_CLAIM_NAMES_TO_USER, user.isToUser());
    claims.put(INTROSPECTION_CLAIM_NAMES_MOBILE, user.getMobile());
    claims.put(INTROSPECTION_CLAIM_NAMES_EMAIL, user.getEmail());
    claims.put(INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID, user.getMainDeptId());
    claims.put(INTROSPECTION_CLAIM_NAMES_PASSWORD_EXPIRED_DATE, user.getPasswordExpiredDate());
    claims.put(INTROSPECTION_CLAIM_NAMES_LAST_MODIFIED_PASSWORD_DATE,
        user.getLastModifiedPasswordDate());
    claims.put(INTROSPECTION_CLAIM_NAMES_EXPIRED_DATE, user.getExpiredDate());
    claims.put(INTROSPECTION_CLAIM_NAMES_TENANT_ID, user.getTenantId());
    claims.put(INTROSPECTION_CLAIM_NAMES_TENANT_NAME, user.getTenantName());
    claims.put(INTROSPECTION_CLAIM_NAMES_TENANT_REAL_NAME_STATUS, user.getTenantRealNameStatus());
    claims.put(INTROSPECTION_CLAIM_NAMES_COUNTRY, user.getCountry());
    //claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_ID, user.getClientId());
    //claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE, user.getClientSource());
    claims.put(INTROSPECTION_CLAIM_NAMES_DIRECTORY_ID, user.getDirectoryId());
    claims.put(INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE, user.getDefaultLanguage());
    claims.put(INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE, user.getDefaultTimeZone());
    claims.put(INTROSPECTION_CLAIM_NAMES_IS_USER_TOKEN, nonNull(userTokenName));

    HttpServletRequest request = ((ServletRequestAttributes) getRequestAttributes()).getRequest();
    String requestId = request.getHeader(Header.REQUEST_ID);
    if (isNotEmpty(requestId)) {
      claims.put(INTROSPECTION_CLAIM_NAMES_ROOT_REQUEST_id, requestId);
      claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT,
          stringSafe(getRequestStringAttribute(requestId, USER_AGENT)));
      claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID,
          stringSafe(getRequestStringAttribute(requestId, DEVICE_ID_IN_QUERY)));
      claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR,
          stringSafe(getRequestStringAttribute(requestId, REMOTE_ADDR_IN_QUERY)));
    }
    return claims;
  }

  private static Map<String, Object> toClientPrincipalClaim(CustomOAuth2RegisteredClient client) {
    Map<String, Object> claims = new HashMap<>();
    //claims.put(INTROSPECTION_CLAIM_NAMES_ID, client.getId());
    claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_ID, client.getClientId());
    claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_NAME, client.getClientName());
    claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_ID_ISSUED_AT, client.getClientIdIssuedAt());
    claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_SECRET_EXPIRES_AT,
        client.getClientSecretExpiresAt());
    claims.put(INTROSPECTION_CLAIM_NAMES_SCOPES, client.getScopes());
    claims.put(INTROSPECTION_CLAIM_NAMES_DESCRIPTION, client.getDescription());
    claims.put(INTROSPECTION_CLAIM_NAMES_PLATFORM, client.getPlatform());
    claims.put(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE, client.getSource());
    claims.put(INTROSPECTION_CLAIM_NAMES_BIZ_TAG, client.getBizTag());
    claims.put(INTROSPECTION_CLAIM_NAMES_TENANT_ID, client.getTenantId());
    claims.put(INTROSPECTION_CLAIM_NAMES_TENANT_NAME, client.getTenantName());

    HttpServletRequest request = ((ServletRequestAttributes) getRequestAttributes()).getRequest();
    claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT, request.getHeader(User_Agent.getValue()));
    claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID, request.getHeader(AUTH_DEVICE_ID));
    claims.put(INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR, request.getRemoteAddr());
    return claims;
  }

  private static Map<String, Object> convertClaimsIfNecessary(Map<String, Object> claims) {
    Map<String, Object> convertedClaims = new HashMap<>(claims);

    Object value = claims.get(OAuth2TokenIntrospectionClaimNames.ISS);
    if (value != null && !(value instanceof URL)) {
      URL convertedValue = ClaimConversionService.getSharedInstance().convert(value, URL.class);
      convertedClaims.put(OAuth2TokenIntrospectionClaimNames.ISS, convertedValue);
    }

    value = claims.get(OAuth2TokenIntrospectionClaimNames.SCOPE);
    if (value != null && !(value instanceof List)) {
      Object convertedValue = ClaimConversionService.getSharedInstance()
          .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
      convertedClaims.put(OAuth2TokenIntrospectionClaimNames.SCOPE, convertedValue);
    }

    value = claims.get(OAuth2TokenIntrospectionClaimNames.AUD);
    if (value != null && !(value instanceof List)) {
      Object convertedValue = ClaimConversionService.getSharedInstance()
          .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
      convertedClaims.put(OAuth2TokenIntrospectionClaimNames.AUD, convertedValue);
    }

    return convertedClaims;
  }

}
