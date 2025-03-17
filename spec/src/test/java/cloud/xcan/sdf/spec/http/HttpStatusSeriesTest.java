package cloud.xcan.sdf.spec.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link HttpStatusSeries}
 */
class HttpStatusSeriesTest {

  @ParameterizedTest
  @CsvSource({"100, INFORMATIONAL", "199, INFORMATIONAL", "200, SUCCESS", "204, SUCCESS",
      "301, REDIRECTION", "404, CLIENT_ERROR", "500, SERVER_ERROR", "777, UNKNOWN"})
  void resolveStatus(int code, HttpStatusSeries expected) {
    assertThat(HttpStatusSeries.valueOf(code)).isEqualTo(expected);
  }

  @Test
  void containsStatusCode() {
    assertThat(HttpStatusSeries.CLIENT_ERROR.contains(401)).isTrue();
  }

  @Test
  void doesNotContainStatusCode() {
    assertThat(HttpStatusSeries.CLIENT_ERROR.contains(200)).isFalse();
  }
}
