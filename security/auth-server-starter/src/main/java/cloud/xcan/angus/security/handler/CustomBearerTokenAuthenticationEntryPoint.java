package cloud.xcan.angus.security.handler;

import static cloud.xcan.angus.remote.message.http.Unauthorized.M.INVALID_PASSWORD;
import static cloud.xcan.angus.remote.message.http.Unauthorized.M.INVALID_PASSWORD_KEY;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.http.ContentType;
import cloud.xcan.angus.spec.locale.MessageHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;

/**
 * An {@link AuthenticationEntryPoint} implementation used to commence authentication of protected
 * resource requests using {@link BearerTokenAuthenticationFilter}.
 * <p>
 * Uses information provided by {@link BearerTokenError} to set HTTP response status code and
 * populate {@code WWW-Authenticate} HTTP header.
 *
 * @see BearerTokenError
 * @see BearerTokenAuthenticationEntryPoint
 * @see <a href="https://tools.ietf.org/html/rfc6750#section-3" target="_blank">RFC 6750
 * Section 3: The WWW-Authenticate Response Header Field</a>
 */
public final class CustomBearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private String realmName;

  private final ObjectMapper objectMapper;

  public CustomBearerTokenAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Collect error details from the provided parameters and format according to RFC 6750,
   * specifically {@code error}, {@code error_description}, {@code error_uri}, and {@code scope}.
   *
   * @param request       that resulted in an <code>AuthenticationException</code>
   * @param response      so that the user agent can begin authentication
   * @param authException that caused the invocation
   */
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws ServletException{
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    Map<String, String> parameters = new LinkedHashMap<>();
    Map<String, String> body = new LinkedHashMap<>();
    if (this.realmName != null) {
      parameters.put("realm", this.realmName);
    }
    if (authException instanceof OAuth2AuthenticationException) {
      OAuth2Error error = ((OAuth2AuthenticationException) authException).getError();
      parameters.put("error", error.getErrorCode());
      body.put("error", error.getErrorCode());
      if (StringUtils.hasText(error.getDescription())) {
        parameters.put("error_description", error.getDescription());
        body.put("error_description", error.getDescription());
      }
      if (StringUtils.hasText(error.getUri())) {
        parameters.put("error_uri", error.getUri());
      }
      if (error instanceof BearerTokenError bearerTokenError) {
        if (StringUtils.hasText(bearerTokenError.getScope())) {
          parameters.put("scope", bearerTokenError.getScope());
        }
        status = ((BearerTokenError) error).getHttpStatus();
      }
    } else if (authException instanceof BadCredentialsException) {
      body.put("error", INVALID_PASSWORD_KEY);
      body.put("error_description", MessageHolder.message(INVALID_PASSWORD));
    }
    String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
    response.addHeader(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
    writeJsonUtf8Result(objectMapper, response, status.value(), body);
  }

  /**
   * Set the default realm name to use in the bearer token error response
   */
  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }

  private static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
    StringBuilder wwwAuthenticate = new StringBuilder();
    wwwAuthenticate.append("Bearer");
    if (!parameters.isEmpty()) {
      wwwAuthenticate.append(" ");
      int i = 0;
      for (Map.Entry<String, String> entry : parameters.entrySet()) {
        wwwAuthenticate.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        if (i != parameters.size() - 1) {
          wwwAuthenticate.append(", ");
        }
        i++;
      }
    }
    return wwwAuthenticate.toString();
  }

  public static void writeJsonUtf8Result(ObjectMapper objectMapper,
      HttpServletResponse response, int status, Object result) throws ServletException {
    response.setCharacterEncoding(DEFAULT_ENCODING);
    response.setContentType(ContentType.TYPE_JSON_UTF8);
    response.setStatus(status);
    try {
      if (nonNull(result)) {
        response.getWriter().write(objectMapper.writeValueAsString(result));
        response.getWriter().flush();
      }
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
  }
}
