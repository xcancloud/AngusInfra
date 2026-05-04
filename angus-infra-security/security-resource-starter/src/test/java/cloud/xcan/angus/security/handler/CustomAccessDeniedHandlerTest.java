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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@DisplayName("CustomAccessDeniedHandler Tests")
@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private ObjectMapper objectMapper;
  private CustomAccessDeniedHandler handler;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    handler = new CustomAccessDeniedHandler(objectMapper);
  }

  @Test
  @DisplayName("sets HTTP 403 Forbidden status")
  void setsForbiddenStatus() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    handler.handle(request, response,
        new AccessDeniedException("Access is denied"));

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  @DisplayName("writes JSON response with UTF-8 encoding")
  void writesJsonResponse() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    handler.handle(request, response,
        new AccessDeniedException("Access is denied"));

    verify(response).setCharacterEncoding("UTF-8");
    verify(response).setContentType("application/json; charset=UTF-8");

    printWriter.flush();
    String jsonOutput = stringWriter.toString();
    assertThat(jsonOutput).isNotEmpty();
    assertThat(jsonOutput).contains("\"code\"");
    assertThat(jsonOutput).contains("Access is denied");
  }

  @Test
  @DisplayName("includes error key 'forbidden' in response extensions")
  void includesForbiddenErrorKey() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    handler.handle(request, response,
        new AccessDeniedException("Custom denial message"));

    printWriter.flush();
    String jsonOutput = stringWriter.toString();
    assertThat(jsonOutput).contains("forbidden");
    assertThat(jsonOutput).contains("Custom denial message");
  }

  @Test
  @DisplayName("sets correct content type header")
  void setsContentType() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    handler.handle(request, response, new AccessDeniedException("denied"));

    ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).setContentType(contentTypeCaptor.capture());
    assertThat(contentTypeCaptor.getValue()).contains("application/json");
  }

  @Test
  @DisplayName("writes valid JSON that can be parsed")
  void writesValidJson() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    handler.handle(request, response, new AccessDeniedException("denied"));

    printWriter.flush();
    String jsonOutput = stringWriter.toString();

    // Verify it's valid JSON by parsing
    ObjectMapper verifier = new ObjectMapper();
    Object parsed = verifier.readValue(jsonOutput, Object.class);
    assertThat(parsed).isNotNull();
  }
}
