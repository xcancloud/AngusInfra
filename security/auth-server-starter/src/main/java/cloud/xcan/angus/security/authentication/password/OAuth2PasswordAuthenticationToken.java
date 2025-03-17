package cloud.xcan.angus.security.authentication.password;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

/**
 * An {@link Authentication} implementation used for the OAuth 2.0 Password Grant.
 */
public class OAuth2PasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

  private final String account;
  private final Set<String> scopes;

  /**
   * Constructs an {@code OAuth2PasswordAuthenticationToken} using the provided parameters.
   *
   * @param account              User username, mobile or email
   * @param clientPrincipal      the authenticated client principal
   * @param scopes               the requested scope(s)
   * @param additionalParameters the additional parameters
   */
  public OAuth2PasswordAuthenticationToken(String account, Authentication clientPrincipal,
      @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {
    super(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters);
    this.account = account;
    this.scopes = Collections.unmodifiableSet(
        (scopes != null) ? new HashSet<>(scopes) : Collections.emptySet());
  }

  /**
   * Returns the requested account.
   *
   * @return the requested user username, mobile or email
   */
  public String getAccount() {
    return account;
  }

  /**
   * Returns the requested scope(s).
   *
   * @return the requested scope(s), or an empty {@code Set} if not available
   */
  public Set<String> getScopes() {
    return this.scopes;
  }

}
