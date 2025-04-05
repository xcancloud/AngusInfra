package cloud.xcan.angus.spec.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HttpSender.Request}.
 */
class RequestTest {

  @Test
  void compressShouldAddContentEncodingHeader()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    HttpSender sender = mock(HttpSender.class);
    HttpSender.Request.Builder builder = HttpSender.Request.build("http://www.xcan.cloud", sender)
        .compress();
    Field requestHeadersField = HttpSender.Request.Builder.class.getDeclaredField("requestHeaders");
    requestHeadersField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, String> requestHeaders = (Map<String, String>) requestHeadersField.get(builder);
    assertThat(requestHeaders).containsEntry("Content-Encoding", "gzip");
  }

}
