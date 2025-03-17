package cloud.xcan.angus.security.handler;

import static cloud.xcan.sdf.api.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.sdf.api.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.INVALID_GRANT_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.PROTOCOL_ERROR_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_GRANT_TYPE_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_RESPONSE_TYPE_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.USER_DENIED_AUTHORIZATION_KEY;
import static cloud.xcan.sdf.api.message.http.Unauthorized.M.INVALID_CLIENT_KEY;
import static cloud.xcan.sdf.api.message.http.Unauthorized.M.INVALID_TOKEN_KEY;
import static cloud.xcan.sdf.api.message.http.Unauthorized.M.UNAUTHORIZED;
import static cloud.xcan.sdf.api.message.http.Unauthorized.M.UNAUTHORIZED_CLIENT_KEY;
import static cloud.xcan.sdf.api.message.http.Unauthorized.M.UNAUTHORIZED_KEY;
import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_ENCODING;
import static java.util.Objects.nonNull;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INSUFFICIENT_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REDIRECT_URI;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_TOKEN;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNAUTHORIZED_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNSUPPORTED_RESPONSE_TYPE;

import cloud.xcan.sdf.api.ApiResult;
import cloud.xcan.sdf.spec.http.ContentType;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.ThrowableAnalyzer;

/**
 * @author XiaoLong Liu
 * @see AuthenticationEntryPoint
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private ThrowableAnalyzer throwableAnalyzer;

  private final ObjectMapper objectMapper;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.throwableAnalyzer = new ThrowableAnalyzer();
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws ServletException {
    log.debug("Translate exception :", authException);
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    // Try to extract a SpringSecurityException from the stacktrace
    Throwable[] causeChain = throwableAnalyzer.determineCauseChain(authException);
    Throwable throwable = throwableAnalyzer.getFirstThrowableOfType(
        OAuth2AuthenticationException.class, causeChain);
    int status = HttpStatus.UNAUTHORIZED.value();
    Map<String, Object> ext = new HashMap<>();
    if (nonNull(throwable)) {
      OAuth2AuthenticationException ase = (OAuth2AuthenticationException) throwable;
      if (INVALID_CLIENT.equals(ase.getError().getErrorCode())) {
        // InvalidClientException
        setWwwAuthenticateHeader(response, ase);
        ext.put(EXT_EKEY_NAME, UNAUTHORIZED_CLIENT_KEY);
      } else if (UNAUTHORIZED_CLIENT.equals(ase.getError().getErrorCode())) {
        // UnauthorizedClientException
        setWwwAuthenticateHeader(response, ase);
        ext.put(EXT_EKEY_NAME, INVALID_CLIENT_KEY);
      } else if (INVALID_GRANT.equals(ase.getError().getErrorCode())) {
        // InvalidGrantException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, INVALID_GRANT_KEY);
      } else if (INVALID_SCOPE.equals(ase.getError().getErrorCode())) {
        // InvalidScopeException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, INVALID_SCOPE);
      } else if (INVALID_TOKEN.equals(ase.getError().getErrorCode())) {
        // InvalidTokenException
        setWwwAuthenticateHeader(response, ase);
        ext.put(EXT_EKEY_NAME, INVALID_TOKEN_KEY);
      } else if (INVALID_REQUEST.equals(ase.getError().getErrorCode())) {
        // InvalidRequestException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, PROTOCOL_ERROR_KEY);
      } else if (INVALID_REDIRECT_URI.equals(ase.getError().getErrorCode())) {
        // RedirectMismatchException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, INVALID_GRANT_KEY);
      } else if (UNSUPPORTED_GRANT_TYPE.equals(ase.getError().getErrorCode())) {
        // UnsupportedGrantTypeException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, UNSUPPORTED_GRANT_TYPE_KEY);
      } else if (UNSUPPORTED_RESPONSE_TYPE.equals(ase.getError().getErrorCode())) {
        // UnsupportedResponseTypeException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, UNSUPPORTED_RESPONSE_TYPE_KEY);
      } else if (ACCESS_DENIED.equals(ase.getError().getErrorCode())) {
        // UserDeniedAuthorizationException
        status = HttpStatus.BAD_REQUEST.value();
        ext.put(EXT_EKEY_NAME, USER_DENIED_AUTHORIZATION_KEY);
      } else if (INSUFFICIENT_SCOPE.equals(ase.getError().getErrorCode())) {
        // InsufficientScopeException
        setWwwAuthenticateHeader(response, ase);
        status = HttpStatus.FORBIDDEN.value();
        ext.put(EXT_EKEY_NAME, INSUFFICIENT_SCOPE);
      } else {
        // OAuth2Exception Base
        ext.put(EXT_EKEY_NAME, PROTOCOL_ERROR_KEY);
      }
    } else {
      ext.put(EXT_EKEY_NAME, UNAUTHORIZED_KEY);
    }
    ApiResult<?> apiResult = ApiResult.error(PROTOCOL_ERROR_CODE,
        MessageHolder.message(UNAUTHORIZED), authException.getMessage(), ext);
    writeJsonUtf8Result(objectMapper, response, status, apiResult);
  }

  private void setWwwAuthenticateHeader(HttpServletResponse response,
      OAuth2AuthenticationException e) {
    response.setHeader("WWW-Authenticate",
        String.format("%s %s", e.getError().getErrorCode(), e.getError().getDescription()));
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
