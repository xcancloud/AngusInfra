/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.xcan.angus.security.authentication.password;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationRequestUtils.ACCESS_TOKEN_REQUEST_ERROR_URI;
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
   * User username, mobile or email.
   */
  public static final String ACCOUNT = "account";

  @Nullable
  @Override
  public Authentication convert(HttpServletRequest request) {
    MultiValueMap<String, String> parameters = OAuth2PasswordAuthenticationRequestUtils.getFormParameters(
        request);

    // grant_type (REQUIRED)
    String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
    if (!AuthorizationGrantType.PASSWORD.getValue().equals(grantType)) {
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
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.USERNAME,
          ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

    // password (REQUIRED)
    String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
    if (!hasText(password) || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
      throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.PASSWORD,
          ACCESS_TOKEN_REQUEST_ERROR_URI);
    }

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

    return new OAuth2PasswordAuthenticationToken(account, clientPrincipal, requestedScopes,
        additionalParameters);
  }

}
