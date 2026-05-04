package cloud.xcan.angus.security.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

/**
 * Unit tests for {@link CustomAuthenticationEntryPoint}.
 *
 * <p>MessageHolder.message() returns the key itself when no MessageSource is configured,
 * which is the case in pure unit tests. This is sufficient for verifying the handler behavior.
 */
@DisplayName("CustomAuthenticationEntryPoint Tests")
@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private ObjectMapper objectMapper;
  private CustomAuthenticationEntryPoint entryPoint;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    entryPoint = new CustomAuthenticationEntryPoint(objectMapper);
  }

  private StringWriter setupResponseWriter() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    return stringWriter;
  }

  @Nested
  @DisplayName("Non-OAuth2 Exceptions")
  class NonOAuth2Exceptions {

    @Test
    @DisplayName("returns 401 for generic AuthenticationException")
    void returns401ForGenericAuthException() throws Exception {
      setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("Bad credentials"));

      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setHeader("Cache-Control", "no-store");
      verify(response).setHeader("Pragma", "no-cache");
    }

    @Test
    @DisplayName("writes JSON response body for generic AuthenticationException")
    void writesJsonBodyForGenericAuthException() throws Exception {
      StringWriter stringWriter = setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("Invalid password"));

      String jsonOutput = stringWriter.toString();
      assertThat(jsonOutput).isNotEmpty();
      assertThat(jsonOutput).contains("Invalid password");
      assertThat(jsonOutput).contains("\"code\"");
    }

    @Test
    @DisplayName("includes 'unauthorized' error key for non-OAuth2 exceptions")
    void includesUnauthorizedEkey() throws Exception {
      StringWriter stringWriter = setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("test"));

      String jsonOutput = stringWriter.toString();
      assertThat(jsonOutput).contains("unauthorized");
    }
  }

  @Nested
  @DisplayName("OAuth2 Exceptions")
  class OAuth2Exceptions {

    @Test
    @DisplayName("returns 401 with WWW-Authenticate header for invalid_client error")
    void handlesInvalidClientError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
          "Client authentication failed", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Client authentication failed");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setHeader("WWW-Authenticate",
          "invalid_client Client authentication failed");
    }

    @Test
    @DisplayName("returns 401 with WWW-Authenticate header for invalid_token error")
    void handlesInvalidTokenError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
          "Token has expired", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Token has expired");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setHeader("WWW-Authenticate",
          "invalid_token Token has expired");
    }

    @Test
    @DisplayName("returns 400 for invalid_grant error")
    void handlesInvalidGrantError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
          "Grant is invalid", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Grant is invalid");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for unsupported_grant_type error")
    void handlesUnsupportedGrantTypeError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE,
          "Grant type not supported", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Grant type not supported");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for invalid_scope error")
    void handlesInvalidScopeError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE,
          "Scope not valid", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Scope not valid");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for invalid_request error")
    void handlesInvalidRequestError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
          "Request is invalid", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Request is invalid");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for invalid_redirect_uri error")
    void handlesInvalidRedirectUriError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REDIRECT_URI,
          "Redirect URI mismatch", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Redirect URI mismatch");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for unsupported_response_type error")
    void handlesUnsupportedResponseTypeError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNSUPPORTED_RESPONSE_TYPE,
          "Response type not supported", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Response type not supported");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 400 for access_denied error")
    void handlesAccessDeniedError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED,
          "Access denied", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Access denied");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("returns 403 for insufficient_scope error")
    void handlesInsufficientScopeError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INSUFFICIENT_SCOPE,
          "Insufficient scope", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Insufficient scope");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.FORBIDDEN.value());
      verify(response).setHeader("WWW-Authenticate",
          "insufficient_scope Insufficient scope");
    }

    @Test
    @DisplayName("returns 401 with WWW-Authenticate for unauthorized_client error")
    void handlesUnauthorizedClientError() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
          "Client not authorized", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Client not authorized");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setHeader("WWW-Authenticate",
          "unauthorized_client Client not authorized");
    }

    @Test
    @DisplayName("returns 401 for unknown OAuth2 error code")
    void handlesUnknownOAuth2ErrorCode() throws Exception {
      setupResponseWriter();

      OAuth2Error error = new OAuth2Error("unknown_error",
          "Something unknown happened", null);
      OAuth2AuthenticationException oAuth2Exception =
          new OAuth2AuthenticationException(error, "Something unknown happened");

      entryPoint.commence(request, response, oAuth2Exception);

      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }
  }

  @Nested
  @DisplayName("Response Format")
  class ResponseFormat {

    @Test
    @DisplayName("sets Cache-Control and Pragma no-cache headers")
    void setsCacheHeaders() throws Exception {
      setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("test"));

      verify(response).setHeader("Cache-Control", "no-store");
      verify(response).setHeader("Pragma", "no-cache");
    }

    @Test
    @DisplayName("sets UTF-8 encoding and JSON content type")
    void setsEncodingAndContentType() throws Exception {
      setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("test"));

      verify(response).setCharacterEncoding("UTF-8");
      verify(response).setContentType("application/json; charset=UTF-8");
    }

    @Test
    @DisplayName("writes valid JSON to response body")
    void writesValidJson() throws Exception {
      StringWriter stringWriter = setupResponseWriter();

      entryPoint.commence(request, response,
          new BadCredentialsException("Bad creds"));

      String jsonOutput = stringWriter.toString();
      assertThat(jsonOutput).isNotEmpty();

      // Verify it's valid JSON by parsing it
      ObjectMapper verifier = new ObjectMapper();
      Object parsed = verifier.readValue(jsonOutput, Object.class);
      assertThat(parsed).isNotNull();
    }
  }

  @Nested
  @DisplayName("writeJsonUtf8Result() static method")
  class WriteJsonUtf8Result {

    @Test
    @DisplayName("writes null result without error")
    void writesNullResultWithoutError() throws Exception {
      CustomAuthenticationEntryPoint.writeJsonUtf8Result(
          objectMapper, response, 200, null);

      verify(response).setStatus(200);
      verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    @DisplayName("writes non-null result as JSON")
    void writesNonNullResult() throws Exception {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      when(response.getWriter()).thenReturn(printWriter);

      CustomAuthenticationEntryPoint.writeJsonUtf8Result(
          objectMapper, response, 403, java.util.Map.of("key", "value"));

      printWriter.flush();
      String output = stringWriter.toString();
      assertThat(output).contains("key");
      assertThat(output).contains("value");
      verify(response).setStatus(403);
    }
  }
}
