package cloud.xcan.angus.observability.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class ApiLogFilterStreamingPathTest {

  @Test
  void detectsAgentChatStreamPath() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    assertThat(ApiLogFilter.isStreamingPath("/api/v1/agents/chat/stream", request)).isTrue();
  }

  @Test
  void detectsChatCompletionsPath() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    assertThat(ApiLogFilter.isStreamingPath("/api/v1/chat/completions", request)).isTrue();
    assertThat(ApiLogFilter.isStreamingPath(
        "/pubapi/v1/application/share/x/chat/completions", request)).isTrue();
  }

  @Test
  void detectsAcceptEventStream() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("text/event-stream");
    assertThat(ApiLogFilter.isStreamingPath("/api/v1/other", request)).isTrue();
  }

  @Test
  void ignoresOrdinaryApi() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("application/json");
    assertThat(ApiLogFilter.isStreamingPath("/api/v1/chat/messages", request)).isFalse();
  }
}
