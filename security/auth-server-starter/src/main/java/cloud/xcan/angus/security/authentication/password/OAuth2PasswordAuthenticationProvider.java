package cloud.xcan.angus.security.authentication.password;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProviderUtils.createHash;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * An {@link AuthenticationProvider} implementation for the OAuth 2.0 Password Grant.
 */
@Slf4j
public final class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

  private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

  private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(
      OidcParameterNames.ID_TOKEN);

  private final OAuth2AuthorizationService authorizationService;
  private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
  private final AuthenticationManager authenticationManager;
  private Consumer<OAuth2PasswordAuthenticationContext> authenticationValidator = new OAuth2PasswordAuthenticationValidator();

  private SessionRegistry sessionRegistry;

  /**
   * Constructs an {@code OAuth2AuthorizationCodeAuthenticationProvider} using the provided
   * parameters.
   *
   * @param authorizationService  the authorization service
   * @param tokenGenerator        the token generator
   * @param authenticationManager for processes an Authentication request.
   */
  public OAuth2PasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
      OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
      AuthenticationManager authenticationManager) {
    Assert.notNull(authorizationService, "authorizationService cannot be null");
    Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
    Assert.notNull(authenticationManager, "authenticationManager cannot be null");
    this.authorizationService = authorizationService;
    this.tokenGenerator = tokenGenerator;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    OAuth2PasswordAuthenticationToken passwordAuthenticationToken = (OAuth2PasswordAuthenticationToken) authentication;

    // Retrieved and checked client?
    OAuth2ClientAuthenticationToken clientPrincipal = OAuth2PasswordAuthenticationProviderUtils
        .getAuthenticatedClientElseThrowInvalidClient(passwordAuthenticationToken);
    RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

    if (log.isTraceEnabled()) {
      log.trace("Retrieved registered client");
    }

    assert registeredClient != null;
    if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
      if (log.isDebugEnabled()) {
        log.debug(String.format(
            "Invalid request: requested grant_type is not allowed" + " for registered client '%s'",
            registeredClient.getId()));
      }
      throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
    }

    // Check the requested scope is allowed
    OAuth2PasswordAuthenticationContext authenticationContext = OAuth2PasswordAuthenticationContext
        .with(passwordAuthenticationToken)
        .registeredClient(registeredClient)
        .build();
    this.authenticationValidator.accept(authenticationContext);

    if (log.isTraceEnabled()) {
      log.trace("Validated token request parameters");
    }

    Authentication passwordAuthentication = OAuth2PasswordAuthenticationProviderUtils
        .passwordAuthentication(authenticationManager, passwordAuthenticationToken);

    Set<String> authorizedScopes = new LinkedHashSet<>(passwordAuthenticationToken.getScopes());
    DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
        .registeredClient(registeredClient)
        .principal(passwordAuthentication)
        .authorizationServerContext(AuthorizationServerContextHolder.getContext())
        .authorizedScopes(authorizedScopes)
        .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .authorizationGrant(passwordAuthenticationToken);

    // ----- Access token -----
    OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .build();
    OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
    if (generatedAccessToken == null) {
      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
          "The token generator failed to generate the access token.", ERROR_URI);
      throw new OAuth2AuthenticationException(error);
    }
    if (log.isTraceEnabled()) {
      log.trace("Generated access token");
    }
    OAuth2AccessToken accessToken = OAuth2PasswordAuthenticationProviderUtils
        .accessToken(generatedAccessToken, tokenContext);

    OAuth2Authorization.Builder authorizationBuilder
        = OAuth2Authorization.withRegisteredClient(registeredClient)
        .principalName(passwordAuthentication.getName())
        .authorizationGrantType(AuthorizationGrantType.PASSWORD)
        .attribute(OAuth2ParameterNames.SCOPE, authorizedScopes)
        .attribute(Principal.class.getName(), passwordAuthentication);

    if (generatedAccessToken instanceof ClaimAccessor) {
      authorizationBuilder.token(accessToken, (metadata) ->
          metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
              ((ClaimAccessor) generatedAccessToken).getClaims()));
    } else {
      authorizationBuilder.accessToken(accessToken);
    }

    // ----- Refresh token -----
    OAuth2RefreshToken refreshToken = null;
    // Do not issue refresh token to public client
    if (registeredClient.getAuthorizationGrantTypes()
        .contains(AuthorizationGrantType.REFRESH_TOKEN)) {
      tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
      OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
      if (generatedRefreshToken != null) {
        if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
          OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
              "The token generator failed to generate a valid refresh token.", ERROR_URI);
          throw new OAuth2AuthenticationException(error);
        }

        if (log.isTraceEnabled()) {
          log.trace("Generated refresh token");
        }

        refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
        authorizationBuilder.refreshToken(refreshToken);
      }
    }

    // ----- ID token -----
    OidcIdToken idToken;
    if (authorizedScopes.contains(OidcScopes.OPENID)) {
      SessionInformation sessionInformation = getSessionInformation(passwordAuthentication);
      if (sessionInformation != null) {
        try {
          // Compute (and use) hash for Session ID
          sessionInformation = new SessionInformation(sessionInformation.getPrincipal(),
              createHash(sessionInformation.getSessionId()), sessionInformation.getLastRequest());
        } catch (NoSuchAlgorithmException ex) {
          OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
              "Failed to compute hash for Session ID.", ERROR_URI);
          throw new OAuth2AuthenticationException(error);
        }
        tokenContextBuilder.put(SessionInformation.class, sessionInformation);
      }
      // @formatter:off
      tokenContext = tokenContextBuilder
          .tokenType(ID_TOKEN_TOKEN_TYPE)
          .authorization(authorizationBuilder.build())	// ID token customizer may need access to the access token and/or refresh token
          .build();
      // @formatter:on
      OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
      if (!(generatedIdToken instanceof Jwt)) {
        OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
            "The token generator failed to generate the ID token.", ERROR_URI);
        throw new OAuth2AuthenticationException(error);
      }

      if (log.isTraceEnabled()) {
        log.trace("Generated id token");
      }

      idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
          generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
      authorizationBuilder.token(idToken,
          (metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
              idToken.getClaims()));
    } else {
      idToken = null;
    }

    OAuth2Authorization authorization = authorizationBuilder.build();

    this.authorizationService.save(authorization);

    if (log.isTraceEnabled()) {
      log.trace("Saved authorization");
    }

    Map<String, Object> additionalParameters = Collections.emptyMap();
    if (idToken != null) {
      additionalParameters = new HashMap<>();
      additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
    }

    if (log.isTraceEnabled()) {
      log.trace("Authenticated token request");
    }

    return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken,
        refreshToken, additionalParameters);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return OAuth2PasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  /**
   * Sets the {@link SessionRegistry} used to track OpenID Connect sessions.
   *
   * @param sessionRegistry the {@link SessionRegistry} used to track OpenID Connect sessions
   */
  public void setSessionRegistry(SessionRegistry sessionRegistry) {
    Assert.notNull(sessionRegistry, "sessionRegistry cannot be null");
    this.sessionRegistry = sessionRegistry;
  }

  /**
   * Sets the {@code Consumer} providing access to the {@link OAuth2PasswordAuthenticationContext}
   * and is responsible for validating specific OAuth 2.0 Client Credentials Grant Request
   * parameters associated in the {@link OAuth2PasswordAuthenticationToken}. The default
   * authentication validator is {@link OAuth2PasswordAuthenticationValidator}.
   *
   * <p>
   * <b>NOTE:</b> The authentication validator MUST throw
   * {@link OAuth2AuthenticationException} if validation fails.
   *
   * @param authenticationValidator the {@code Consumer} providing access to the
   *                                {@link OAuth2PasswordAuthenticationContext} and is responsible
   *                                for validating specific OAuth 2.0 Client Credentials Grant
   *                                Request parameters
   */
  public void setAuthenticationValidator(
      Consumer<OAuth2PasswordAuthenticationContext> authenticationValidator) {
    Assert.notNull(authenticationValidator, "authenticationValidator cannot be null");
    this.authenticationValidator = authenticationValidator;
  }

  private SessionInformation getSessionInformation(Authentication principal) {
    SessionInformation sessionInformation = null;
    if (this.sessionRegistry != null) {
      List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(
          principal.getPrincipal(), false);
      if (!CollectionUtils.isEmpty(sessions)) {
        sessionInformation = sessions.get(0);
        if (sessions.size() > 1) {
          // Get the most recent session
          sessions = new ArrayList<>(sessions);
          sessions.sort(Comparator.comparing(SessionInformation::getLastRequest));
          sessionInformation = sessions.get(sessions.size() - 1);
        }
      }
    }
    return sessionInformation;
  }

}
