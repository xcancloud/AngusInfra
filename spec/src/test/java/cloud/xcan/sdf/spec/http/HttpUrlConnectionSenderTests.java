package cloud.xcan.angus.spec.http;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;

@ExtendWith(WiremockResolver.class)
class HttpUrlConnectionSenderTests {

  HttpSender httpSender = new HttpUrlConnectionSender();

  @Test
  void customReadTimeoutHonored(@WiremockResolver.Wiremock WireMockServer server) throws Throwable {
    this.httpSender = new HttpUrlConnectionSender(Duration.ofSeconds(1), Duration.ofMillis(1));
    server.stubFor(any(urlEqualTo("/endpoint")).willReturn(ok().withFixedDelay(5)));

    assertThatExceptionOfType(SocketTimeoutException.class)
        .isThrownBy(() -> httpSender.post(server.baseUrl() + "/endpoint").send());
  }
}
