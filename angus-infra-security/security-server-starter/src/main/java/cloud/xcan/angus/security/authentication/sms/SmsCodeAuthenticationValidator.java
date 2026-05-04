package cloud.xcan.angus.security.authentication.sms;

import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * A {@code Consumer} providing access to the {@link SmsCodeAuthenticationContext} containing an
 * {@link SmsCodeAuthenticationToken} and is the default
 * {@link SmsCodeAuthenticationProvider#setAuthenticationValidator(Consumer) authentication
 * validator} used for validating specific OAuth 2.0 Password  Grant Request parameters.
 *
 * <p>
 * The default implementation validates {@link SmsCodeAuthenticationToken#getScopes()}. If
 * validation fails, an {@link OAuth2AuthenticationException} is thrown.
 *
 * @see SmsCodeAuthenticationContext
 * @see SmsCodeAuthenticationToken
 * @see SmsCodeAuthenticationProvider#setAuthenticationValidator(Consumer)
 */
@Slf4j
public final class SmsCodeAuthenticationValidator
    implements Consumer<SmsCodeAuthenticationContext> {

  /**
   * The default validator for {@link SmsCodeAuthenticationToken#getScopes()}.
   */
  public static final Consumer<SmsCodeAuthenticationContext> DEFAULT_SCOPE_VALIDATOR = SmsCodeAuthenticationValidator::validateScope;

  private final Consumer<SmsCodeAuthenticationContext> authenticationValidator = DEFAULT_SCOPE_VALIDATOR;

  @Override
  public void accept(SmsCodeAuthenticationContext authenticationContext) {
    this.authenticationValidator.accept(authenticationContext);
  }

  private static void validateScope(SmsCodeAuthenticationContext authenticationContext) {
    SmsCodeAuthenticationToken clientCredentialsAuthentication
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
