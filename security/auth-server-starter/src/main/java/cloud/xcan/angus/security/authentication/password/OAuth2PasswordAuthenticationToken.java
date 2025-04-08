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

  @Nullable
  private final String id;
  private final String account;
  private final Set<String> scopes;

  /**
   * This is usually a password.
   *
   * @see Authentication#getCredentials()
   */
  private final Object credentials;

  /**
   * Constructs an {@code OAuth2PasswordAuthenticationToken} using the provided parameters.
   *
   * @param id                   the user id, identify the unique user. Allow phone numbers and email
   *                             the addresses under multiple tenants, there may be multiple values
   * @param account              the user username, mobile or email
   * @param password             the user password
   * @param clientPrincipal      the authenticated client principal
   * @param scopes               the requested scope(s)
   * @param additionalParameters the additional parameters
   */
  public OAuth2PasswordAuthenticationToken(@Nullable String id, String account, String password,
      Authentication clientPrincipal, @Nullable Set<String> scopes,
      @Nullable Map<String, Object> additionalParameters) {
    super(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters);
    this.id = id;
    this.account = account;
    this.credentials = password;
    this.scopes = Collections.unmodifiableSet(
        (scopes != null) ? new HashSet<>(scopes) : Collections.emptySet());
  }

  /**
   * Returns the requested user id.
   *
   * @return the requested user id
   */
  @Nullable
  public String getId() {
    return id;
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

  /**
   * The credentials that prove the principal is correct. This is usually a password, but could be
   * anything relevant to the <code>AuthenticationManager</code>. Callers are expected to populate
   * the credentials.
   *
   * @return the credentials that prove the identity of the <code>Principal</code>
   */
  public Object getCredentials() {
    return this.credentials;
  }
}
