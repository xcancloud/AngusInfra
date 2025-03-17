package cloud.xcan.angus.security.authentication.password;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

final class OAuth2PasswordAuthenticationRequestUtils {

  static final String ACCESS_TOKEN_REQUEST_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

  private OAuth2PasswordAuthenticationRequestUtils() {
  }

  static MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameterMap.forEach((key, values) -> {
      String queryString =
          StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";
      // If not query parameter then it's a form parameter
      if (!queryString.contains(key)) {
        for (String value : values) {
          parameters.add(key, value);
        }
      }
    });
    return parameters;
  }

  static MultiValueMap<String, String> getQueryParameters(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameterMap.forEach((key, values) -> {
      String queryString =
          StringUtils.hasText(request.getQueryString()) ? request.getQueryString() : "";
      if (queryString.contains(key)) {
        for (String value : values) {
          parameters.add(key, value);
        }
      }
    });
    return parameters;
  }

  static Map<String, Object> getParametersIfMatchesAuthorizationCodeGrantRequest(
      HttpServletRequest request, String... exclusions) {
    if (!matchesAuthorizationCodeGrantRequest(request)) {
      return Collections.emptyMap();
    }
    MultiValueMap<String, String> multiValueParameters = "GET".equals(request.getMethod())
        ? getQueryParameters(request) : getFormParameters(request);
    for (String exclusion : exclusions) {
      multiValueParameters.remove(exclusion);
    }

    Map<String, Object> parameters = new HashMap<>();
    multiValueParameters.forEach(
        (key, value) -> parameters.put(key,
            (value.size() == 1) ? value.get(0) : value.toArray(new String[0])));

    return parameters;
  }

  static boolean matchesAuthorizationCodeGrantRequest(HttpServletRequest request) {
    return AuthorizationGrantType.PASSWORD.getValue()
        .equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE));
  }

  static void throwError(String errorCode, String parameterName, String errorUri) {
    OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
    throw new OAuth2AuthenticationException(error);
  }
}
