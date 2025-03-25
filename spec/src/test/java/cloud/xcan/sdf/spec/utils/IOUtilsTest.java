package cloud.xcan.angus.spec.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IOUtils}.
 */
class IOUtilsTest {

  @Test
  void testToString() {
    String expected = "This is a sample.";

    ByteArrayInputStream inputStream = new ByteArrayInputStream(expected.getBytes());

    assertThat(IOUtils.toString(inputStream)).isEqualTo(expected);
  }

  @Test
  void testToStringWithCharset() {
    String expected = "This is a sample.";

    ByteArrayInputStream inputStream = new ByteArrayInputStream(
        expected.getBytes(StandardCharsets.UTF_8));

    assertThat(IOUtils.toString(inputStream, StandardCharsets.UTF_8)).isEqualTo(expected);
  }

}
