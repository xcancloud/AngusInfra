package cloud.xcan.angus.core.spring.security;

import static cloud.xcan.angus.core.spring.env.EnvKeys.GM_APIS_URL_PREFIX;
import static cloud.xcan.angus.core.spring.env.EnvKeys.OAUTH2_INTROSPECT_CLIENT_ID;
import static cloud.xcan.angus.core.spring.env.EnvKeys.OAUTH2_INTROSPECT_CLIENT_SECRET;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.ACCESS_TOKEN;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.PRINCIPAL;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.spring.env.EnvHelper;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public final class TokenValidator {

  private static final RestTemplate REST_TEMPLATE = new RestTemplate();

  private static final String BEARER_PREFIX = BEARER_TOKEN_TYPE + " ";

  private TokenValidator() {
  }

  public static Map<String, Object> validate(ServerHttpRequest request) {
    if (!(request instanceof ServletServerHttpRequest)) {
      throw new IllegalArgumentException(
          "Token validation requires ServletServerHttpRequest, got: "
              + request.getClass().getName());
    }
    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
    String token = getAccessToken(servletRequest);
    if (isEmpty(token)) {
      throw new AccessDeniedException("Access token is missing");
    }

    Map<String, Object> principal = validate(token,
        request.getHeaders().getFirst(Header.REQUEST_ID));
    if (principal == null) {
      throw new AccessDeniedException("Invalid access token");
    }
    return principal;
  }

  public static Map<String, Object> validate(String token, String requestId) {
    Object url = EnvHelper.getString(GM_APIS_URL_PREFIX);
    if (isNull(url)) {
      throw new IllegalStateException("GM_APIS_URL_PREFIX is not configured");
    }

    String clientId = EnvHelper.getString(OAUTH2_INTROSPECT_CLIENT_ID, "");
    String clientSecret = EnvHelper.getString(OAUTH2_INTROSPECT_CLIENT_SECRET, "");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);
    headers.add(Header.REQUEST_ID, stringSafe(requestId));

    MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("token", token);

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map<String, Object>> response = REST_TEMPLATE.exchange(
        url + "/oauth2/introspect",
        HttpMethod.POST,
        entity,
        new ParameterizedTypeReference<Map<String, Object>>() {
        });

    if (response.getStatusCode() != HttpStatus.OK) {
      return null;
    }
    Map<String, Object> responseBody = response.getBody();
    if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("active"))) {
      return null;
    }
    Object raw = responseBody.get(PRINCIPAL);
    if (raw instanceof Map<?, ?>) {
      @SuppressWarnings("unchecked")
      Map<String, Object> principal = (Map<String, Object>) raw;
      return principal;
    }
    return null;
  }

  public static @Nullable String getAccessToken(ServletServerHttpRequest request) {
    String token = request.getServletRequest().getParameter(ACCESS_TOKEN);
    if (isEmpty(token)) {
      String authHeader = request.getServletRequest().getHeader("Authorization");
      if (isNotEmpty(authHeader) && authHeader.length() > BEARER_PREFIX.length()
          && authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
        token = authHeader.substring(BEARER_PREFIX.length()).trim();
      }
    }
    return token;
  }
}
