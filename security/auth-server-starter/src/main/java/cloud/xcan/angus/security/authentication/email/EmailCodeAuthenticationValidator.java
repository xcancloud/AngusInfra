package cloud.xcan.angus.security.authentication.email;

import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * A {@code Consumer} providing access to the {@link EmailCodeAuthenticationContext} containing an
 * {@link EmailCodeAuthenticationToken} and is the default
 * {@link EmailCodeAuthenticationProvider#setAuthenticationValidator(Consumer) authentication
 * validator} used for validating specific OAuth 2.0 Password  Grant Request parameters.
 *
 * <p>
 * The default implementation validates {@link EmailCodeAuthenticationToken#getScopes()}. If
 * validation fails, an {@link OAuth2AuthenticationException} is thrown.
 *
 * @see EmailCodeAuthenticationContext
 * @see EmailCodeAuthenticationToken
 * @see EmailCodeAuthenticationProvider#setAuthenticationValidator(Consumer)
 */
@Slf4j
public final class EmailCodeAuthenticationValidator
    implements Consumer<EmailCodeAuthenticationContext> {

  /**
   * The default validator for {@link EmailCodeAuthenticationToken#getScopes()}.
   */
  public static final Consumer<EmailCodeAuthenticationContext> DEFAULT_SCOPE_VALIDATOR = EmailCodeAuthenticationValidator::validateScope;

  private final Consumer<EmailCodeAuthenticationContext> authenticationValidator = DEFAULT_SCOPE_VALIDATOR;

  @Override
  public void accept(EmailCodeAuthenticationContext authenticationContext) {
    this.authenticationValidator.accept(authenticationContext);
  }

  private static void validateScope(EmailCodeAuthenticationContext authenticationContext) {
    EmailCodeAuthenticationToken clientCredentialsAuthentication
        = authenticationContext.getAuthentication();
    RegisteredClient registeredClient = authenticationContext.getRegisteredClient();

    Set<String> requestedScopes = clientCredentialsAuthentication.getScopes();
    Set<String> allowedScopes = registeredClient.getScopes();
    if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
      if (log.isDebugEnabled()) {
        log.debug(String.format(
            "Invalid request: requested scope is not allowed" + " for registered client '%s'",
            registeredClient.getId()));
      }
      throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
    }
  }

}
