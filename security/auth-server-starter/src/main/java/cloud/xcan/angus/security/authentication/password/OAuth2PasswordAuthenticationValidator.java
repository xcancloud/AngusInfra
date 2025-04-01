package cloud.xcan.angus.security.authentication.password;

import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * A {@code Consumer} providing access to the {@link OAuth2PasswordAuthenticationContext} containing
 * an {@link OAuth2PasswordAuthenticationToken} and is the default
 * {@link OAuth2PasswordAuthenticationProvider#setAuthenticationValidator(Consumer) authentication
 * validator} used for validating specific OAuth 2.0 Password  Grant Request parameters.
 *
 * <p>
 * The default implementation validates {@link OAuth2PasswordAuthenticationToken#getScopes()}. If
 * validation fails, an {@link OAuth2AuthenticationException} is thrown.
 *
 * @see OAuth2PasswordAuthenticationContext
 * @see OAuth2PasswordAuthenticationToken
 * @see OAuth2PasswordAuthenticationProvider#setAuthenticationValidator(Consumer)
 */
public final class OAuth2PasswordAuthenticationValidator
    implements Consumer<OAuth2PasswordAuthenticationContext> {

  private static final Log LOGGER = LogFactory.getLog(OAuth2PasswordAuthenticationValidator.class);

  /**
   * The default validator for {@link OAuth2PasswordAuthenticationToken#getScopes()}.
   */
  public static final Consumer<OAuth2PasswordAuthenticationContext> DEFAULT_SCOPE_VALIDATOR = OAuth2PasswordAuthenticationValidator::validateScope;

  private final Consumer<OAuth2PasswordAuthenticationContext> authenticationValidator = DEFAULT_SCOPE_VALIDATOR;

  @Override
  public void accept(OAuth2PasswordAuthenticationContext authenticationContext) {
    this.authenticationValidator.accept(authenticationContext);
  }

  private static void validateScope(OAuth2PasswordAuthenticationContext authenticationContext) {
    OAuth2PasswordAuthenticationToken clientCredentialsAuthentication
        = authenticationContext.getAuthentication();
    RegisteredClient registeredClient = authenticationContext.getRegisteredClient();

    Set<String> requestedScopes = clientCredentialsAuthentication.getScopes();
    Set<String> allowedScopes = registeredClient.getScopes();
    if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(LogMessage.format(
            "Invalid request: requested scope is not allowed" + " for registered client '%s'",
            registeredClient.getId()));
      }
      throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
    }
  }

}
