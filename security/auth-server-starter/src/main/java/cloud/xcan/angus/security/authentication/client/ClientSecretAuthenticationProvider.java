package cloud.xcan.angus.security.authentication.client;

import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation used for OAuth 2.0 Client Authentication, which
 * authenticates the {@link OAuth2ParameterNames#CLIENT_SECRET client_secret} parameter.
 *
 * <p>
 * Change :: Use CustomOAuth2RegisteredClient instead of RegisteredClient. Avoid
 * CustomOAuth2RegisteredClient being cast to RegisteredClient, resulting in the loss of extended
 * fields.
 *
 * @author Patryk Kostrzewa
 * @author Joe Grandja
 * @author XiaoLong Liu
 * @see AuthenticationProvider
 * @see OAuth2ClientAuthenticationToken
 * @see RegisteredClientRepository
 * @see OAuth2AuthorizationService
 * @see PasswordEncoder
 * @since 0.2.3
 */
public final class ClientSecretAuthenticationProvider implements AuthenticationProvider {

  private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-3.2.1";

  private final Log logger = LogFactory.getLog(getClass());

  private final RegisteredClientRepository registeredClientRepository;

  private final CodeVerifierAuthenticator codeVerifierAuthenticator;

  private PasswordEncoder passwordEncoder;

  /**
   * Constructs a {@code ClientSecretAuthenticationProvider} using the provided parameters.
   *
   * @param registeredClientRepository the repository of registered clients
   * @param authorizationService       the authorization service
   */
  public ClientSecretAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
      OAuth2AuthorizationService authorizationService) {
    Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
    Assert.notNull(authorizationService, "authorizationService cannot be null");
    this.registeredClientRepository = registeredClientRepository;
    this.codeVerifierAuthenticator = new CodeVerifierAuthenticator(authorizationService);
    this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  /**
   * Sets the {@link PasswordEncoder} used to validate the
   * {@link RegisteredClient#getClientSecret() client secret}. If not set, the client secret will be
   * compared using {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}.
   *
   * @param passwordEncoder the {@link PasswordEncoder} used to validate the client secret
   */
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;

    // @formatter:off
		if (!ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(clientAuthentication.getClientAuthenticationMethod()) &&
				!ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(clientAuthentication.getClientAuthenticationMethod())) {
			return null;
		}
		// @formatter:on

    String clientId = clientAuthentication.getPrincipal().toString();
    CustomOAuth2RegisteredClient registeredClient =
        (CustomOAuth2RegisteredClient) this.registeredClientRepository.findByClientId(clientId);
    if (registeredClient == null) {
      throwInvalidClient(OAuth2ParameterNames.CLIENT_ID);
    }

    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Retrieved registered client");
    }

    if (!Objects.requireNonNull(registeredClient).getClientAuthenticationMethods()
        .contains(clientAuthentication.getClientAuthenticationMethod())) {
      throwInvalidClient("authentication_method");
    }

    if (clientAuthentication.getCredentials() == null) {
      throwInvalidClient("credentials");
    }

    String clientSecret = clientAuthentication.getCredentials().toString();

    // When generating user or system access tokens using encrypted client secret internally
    if (clientSecret.startsWith("{") && !clientSecret.equals(registeredClient.getClientSecret())) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug(LogMessage.format(
            "Invalid request: client_secret does not match" + " for registered client '%s'",
            registeredClient.getId()));
      }
      throwInvalidClient(OAuth2ParameterNames.CLIENT_SECRET);
    }

    // When generating access tokens using not encrypted client secret by user
    if (!clientSecret.startsWith("{")
        && !this.passwordEncoder.matches(clientSecret, registeredClient.getClientSecret())) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug(LogMessage.format(
            "Invalid request: client_secret does not match" + " for registered client '%s'",
            registeredClient.getId()));
      }
      throwInvalidClient(OAuth2ParameterNames.CLIENT_SECRET);
    }

    if (registeredClient.getClientSecretExpiresAt() != null
        && Instant.now().isAfter(registeredClient.getClientSecretExpiresAt())) {
      throwInvalidClient("client_secret_expires_at");
    }

    // Fix:: CustomOAuth2RegisteredClient being cast to RegisteredClient, resulting in the loss of extended fields.
    if (this.passwordEncoder.upgradeEncoding(registeredClient.getClientSecret())) {
      registeredClient = /*RegisteredClient*/CustomOAuth2RegisteredClient.from(registeredClient)
          .clientSecret(this.passwordEncoder.encode(clientSecret))
          .build();
      // Will there be any security vulnerability ???
      this.registeredClientRepository.save(registeredClient);
    }

    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Validated client authentication parameters");
    }

    // Validate the "code_verifier" parameter for the confidential client, if
    // available
    this.codeVerifierAuthenticator.authenticateIfAvailable(clientAuthentication, registeredClient);

    if (this.logger.isTraceEnabled()) {
      this.logger.trace("Authenticated client secret");
    }

    return new OAuth2ClientAuthenticationToken(
        registeredClient, clientAuthentication.getClientAuthenticationMethod(),
        clientAuthentication.getCredentials());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private static void throwInvalidClient(String parameterName) {
    OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
        "Client authentication failed: " + parameterName, ERROR_URI);
    throw new OAuth2AuthenticationException(error);
  }

}
