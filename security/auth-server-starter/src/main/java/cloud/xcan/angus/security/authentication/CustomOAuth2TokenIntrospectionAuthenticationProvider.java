package cloud.xcan.angus.security.authentication;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

public final class CustomOAuth2TokenIntrospectionAuthenticationProvider implements
    AuthenticationProvider {

  private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);

  private static final TypeDescriptor LIST_STRING_TYPE_DESCRIPTOR = TypeDescriptor.collection(
      List.class, TypeDescriptor.valueOf(String.class));

  public static final String INTROSPECTION_CLAIM_NAMES_SCOPE = "permissions";

  private final Log logger = LogFactory.getLog(getClass());

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
    OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication = (OAuth2TokenIntrospectionAuthenticationToken) authentication;

    OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(
        tokenIntrospectionAuthentication);

    OAuth2Authorization authorization = this.authorizationService
        .findByToken(tokenIntrospectionAuthentication.getToken(), null);
    if (authorization == null) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(
            "Did not authenticate token introspection request since token was not found");
      }
      // Return the authentication request when token not found
      return tokenIntrospectionAuthentication;
    }

    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Retrieved authorization with token");
    }

    OAuth2Authorization.Token<OAuth2Token> authorizedToken = authorization
        .getToken(tokenIntrospectionAuthentication.getToken());
    if (!authorizedToken.isActive()) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("Did not introspect token since not active");
      }
      return new OAuth2TokenIntrospectionAuthenticationToken(
          tokenIntrospectionAuthentication.getToken(),
          clientPrincipal, OAuth2TokenIntrospection.builder().build());
    }

    RegisteredClient authorizedClient = this.registeredClientRepository
        .findById(authorization.getRegisteredClientId());
    OAuth2TokenIntrospection tokenClaims = withActiveTokenClaims(authorization,
        authorizedToken, authorizedClient);

    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Authenticated token introspection request");
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

    Object principal = authorization.getAttribute(Principal.class.getName());
    if (principal != null && principal instanceof UsernamePasswordAuthenticationToken) {
      User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (ObjectUtils.isNotEmpty(user.getAuthorities())) {
        tokenClaims.claim(INTROSPECTION_CLAIM_NAMES_SCOPE,
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(
                Collectors.toSet()));
      }
    }
    return tokenClaims.build();
  }

  private static Map<String, Object> convertClaimsIfNecessary(Map<String, Object> claims) {
    Map<String, Object> convertedClaims = new HashMap<>(claims);

    Object value = claims.get(OAuth2TokenIntrospectionClaimNames.ISS);
    if (value != null && !(value instanceof URL)) {
      URL convertedValue = ClaimConversionService.getSharedInstance().convert(value, URL.class);
      if (convertedValue != null) {
        convertedClaims.put(OAuth2TokenIntrospectionClaimNames.ISS, convertedValue);
      }
    }

    value = claims.get(OAuth2TokenIntrospectionClaimNames.SCOPE);
    if (value != null && !(value instanceof List)) {
      Object convertedValue = ClaimConversionService.getSharedInstance()
          .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
      if (convertedValue != null) {
        convertedClaims.put(OAuth2TokenIntrospectionClaimNames.SCOPE, convertedValue);
      }
    }

    value = claims.get(OAuth2TokenIntrospectionClaimNames.AUD);
    if (value != null && !(value instanceof List)) {
      Object convertedValue = ClaimConversionService.getSharedInstance()
          .convert(value, OBJECT_TYPE_DESCRIPTOR, LIST_STRING_TYPE_DESCRIPTOR);
      if (convertedValue != null) {
        convertedClaims.put(OAuth2TokenIntrospectionClaimNames.AUD, convertedValue);
      }
    }

    return convertedClaims;
  }

}
