package cloud.xcan.angus.security.authentication.sms;

import static cloud.xcan.angus.api.enums.SignInType.SMS_CODE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class SmsCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

  private final String id;
  private final String mobile; // mobile
  private final Set<String> scopes;

  /**
   * This is usually an SMS verification code.
   *
   * @see Authentication#getCredentials()
   */
  private final Object credentials;

  public static final AuthorizationGrantType SMS_CODE_GRANT_TYPE = new AuthorizationGrantType(
      SMS_CODE.getValue());

  /**
   * Constructs an {@code SmsCodeAuthenticationToken} using the provided parameters.
   *
   * @param id                   the user id, identify the unique user. Allow phone numbers and
   *                             email the addresses under multiple tenants, there may be multiple
   *                             values
   * @param mobile               the user mobile
   * @param password             the linkSecret of SMS verification code
   * @param clientPrincipal      the authenticated client principal
   * @param scopes               the requested scope(s)
   * @param additionalParameters the additional parameters
   */
  public SmsCodeAuthenticationToken(String id, String mobile, String password,
      Authentication clientPrincipal, @Nullable Set<String> scopes,
      @Nullable Map<String, Object> additionalParameters) {
    super(SMS_CODE_GRANT_TYPE, clientPrincipal, additionalParameters);
    this.id = id;
    this.mobile = mobile;
    this.credentials = password;
    this.scopes = Collections.unmodifiableSet(
        (scopes != null) ? new HashSet<>(scopes) : Collections.emptySet());
  }

  /**
   * Returns the requested user id.
   *
   * @return the requested user id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the requested mobile.
   *
   * @return the requested user mobile
   */
  public String getMobile() {
    return mobile;
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
   * The credentials that prove the principal is correct. This is usually a linkSecret of SMS
   * verification code, but could be anything relevant to the <code>AuthenticationManager</code>.
   * Callers are expected to populate the credentials.
   *
   * @return the credentials that prove the identity of the <code>Principal</code>
   */
  public Object getCredentials() {
    return this.credentials;
  }

}
