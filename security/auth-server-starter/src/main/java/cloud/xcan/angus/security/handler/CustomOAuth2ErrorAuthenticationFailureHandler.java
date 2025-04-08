package cloud.xcan.angus.security.handler;

import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REDIRECT_URI;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_TOKEN;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNSUPPORTED_RESPONSE_TYPE;

import cloud.xcan.angus.remote.message.ProtocolException.M;
import cloud.xcan.angus.remote.message.http.Unauthorized;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.Assert;

/**
 * An implementation of an {@link AuthenticationFailureHandler} used for handling an
 * {@link OAuth2AuthenticationException} and returning the
 * {@link OAuth2Error OAuth 2.0 Error Response}.
 *
 * @see AuthenticationFailureHandler
 * @see OAuth2ErrorHttpMessageConverter
 */
public final class CustomOAuth2ErrorAuthenticationFailureHandler implements
    AuthenticationFailureHandler {

  private final Log logger = LogFactory.getLog(getClass());

  private HttpMessageConverter<OAuth2Error> errorResponseConverter = new OAuth2ErrorHttpMessageConverter();

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authenticationException) throws IOException, ServletException {
    ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
    httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);

    if (authenticationException instanceof OAuth2AuthenticationException) {
      OAuth2Error error = ((OAuth2AuthenticationException) authenticationException).getError();
      String errorMessage;
      if (INVALID_GRANT.equals(error.getErrorCode())) {
        // InvalidGrantException
        errorMessage = message(M.INVALID_GRANT);
      } else if (INVALID_SCOPE.equals(error.getErrorCode())) {
        // InvalidScopeException
        errorMessage = message(M.INVALID_SCOPE);
      } else if (INVALID_TOKEN.equals(error.getErrorCode())) {
        // InvalidTokenException
        errorMessage = message(Unauthorized.M.INVALID_TOKEN);
      } else if (INVALID_REQUEST.equals(error.getErrorCode())) {
        // InvalidRequestException
        errorMessage = message(M.PROTOCOL_ERROR);
      } else if (INVALID_REDIRECT_URI.equals(error.getErrorCode())) {
        // RedirectMismatchException
        errorMessage = message(M.INVALID_GRANT);
      } else if (UNSUPPORTED_GRANT_TYPE.equals(error.getErrorCode())) {
        // UnsupportedGrantTypeException
        errorMessage = message(M.UNSUPPORTED_GRANT_TYPE);
      } else if (UNSUPPORTED_RESPONSE_TYPE.equals(error.getErrorCode())) {
        // UnsupportedResponseTypeException
        errorMessage = message(M.UNSUPPORTED_RESPONSE_TYPE);
      } else if (ACCESS_DENIED.equals(error.getErrorCode())) {
        // UserDeniedAuthorizationException
        errorMessage = message(M.USER_DENIED_AUTHORIZATION);
      } /*else if (INSUFFICIENT_SCOPE.equals(error.getErrorCode())) {
        // InsufficientScopeException
        status = HttpStatus.FORBIDDEN.value();
        ext.put(EXT_EKEY_NAME, INSUFFICIENT_SCOPE);
      } */ else {
        // OAuth2Exception Base
        errorMessage = nullSafe(error.getDescription(), message(M.PROTOCOL_ERROR));
      }
      this.errorResponseConverter.write(
          new OAuth2Error(error.getErrorCode(), errorMessage, error.getUri()), null, httpResponse);
    } else {
      if (this.logger.isWarnEnabled()) {
        this.logger.warn(AuthenticationException.class.getSimpleName() + " must be of type "
            + OAuth2AuthenticationException.class.getName() + " but was "
            + authenticationException.getClass().getName());
      }
    }
  }

  /**
   * Sets the {@link HttpMessageConverter} used for converting an {@link OAuth2Error} to an HTTP
   * response.
   *
   * @param errorResponseConverter the {@link HttpMessageConverter} used for converting an
   *                               {@link OAuth2Error} to an HTTP response
   */
  public void setErrorResponseConverter(HttpMessageConverter<OAuth2Error> errorResponseConverter) {
    Assert.notNull(errorResponseConverter, "errorResponseConverter cannot be null");
    this.errorResponseConverter = errorResponseConverter;
  }

}
