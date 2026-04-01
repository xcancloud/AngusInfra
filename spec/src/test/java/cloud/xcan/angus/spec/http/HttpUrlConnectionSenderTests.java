package cloud.xcan.angus.spec.http;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@WireMockTest
class HttpUrlConnectionSenderTests {

  HttpSender httpSender = new HttpUrlConnectionSender();

  @Test
  void customReadTimeoutHonored(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
    this.httpSender = new HttpUrlConnectionSender(Duration.ofSeconds(1), Duration.ofMillis(1));
    stubFor(any(urlEqualTo("/endpoint")).willReturn(ok().withFixedDelay(5)));

    assertThatExceptionOfType(SocketTimeoutException.class)
        .isThrownBy(() -> httpSender.post(wmRuntimeInfo.getHttpBaseUrl() + "/endpoint").send());
  }
}
