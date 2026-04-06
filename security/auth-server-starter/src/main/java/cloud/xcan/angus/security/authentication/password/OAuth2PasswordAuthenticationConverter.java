package cloud.xcan.angus.security.authentication.password;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationRequestUtils.ACCESS_TOKEN_REQUEST_ERROR_URI;
import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationRequestUtils.getFormParameters;
import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationRequestUtils.throwError;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.USERNAME;
import static org.springframework.util.StringUtils.hasText;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Attempts to extract an Access Token Request from {@link HttpServletRequest} for the OAuth 2.0
 * Password Grant and then converts it to an {@link OAuth2PasswordAuthenticationToken} used for
 * authenticating the authorization grant.
 *
 * @see AuthenticationConverter
 * @see OAuth2PasswordAuthenticationToken
 * @see OAuth2TokenEndpointFilter
 */
public final class OAuth2PasswordAuthenticationConverter implements AuthenticationConverter {

  /**
   * User username, phone or email.
   */
  public static final String ACCOUNT = "account";
  public static final String USER_ID = "user_id";

  @Nullable
  @Override
  public Authentication convert(HttpServletRequest request) {
    MultiValueMap<String, String> parameters = getFormParameters(request);

    // grant_type (REQUIRED)
    String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
    if (!AuthorizationGrantType.PASSWORD.getValue().equalsIgnoreCase(grantType)) {
      return null;
    }

    // scope (OPTIONAL)
    String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
    if (hasText(scope) && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.SCOPE,
          ACCESS_TOKEN_REQUEST_ERROR_URI);
    }
    Set<String> requestedScopes = null;
    if (hasText(scope)) {
      requestedScopes = new HashSet<>(
          Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
    }

    // account (REQUIRED)
    String account = parameters.getFirst(ACCOUNT);
    if (isEmpty(account)) {
      // Important! Compatible with old parameter.
      account = request.getParameter(USERNAME);
    }
    if (!hasText(account) /*|| parameters.get(ACCOUNT).size() != 1*/) {
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, ACCOUNT, ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    // password (REQUIRED)
    String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
    if (!hasText(password) || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.PASSWORD,
          ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    // id (User id, OPTIONAL)
    String userId = parameters.getFirst(USER_ID);

    Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

    if (clientPrincipal == null) {
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ErrorCodes.INVALID_CLIENT,
          ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    Map<String, Object> additionalParameters = new HashMap<>();
    parameters.forEach((key, value) -> {
      if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
          !key.equals(OAuth2ParameterNames.SCOPE)) {
        additionalParameters.put(key, value.get(0));
      }
    });

    return new OAuth2PasswordAuthenticationToken(userId, account, password, clientPrincipal,
        requestedScopes, additionalParameters);
  }

}
