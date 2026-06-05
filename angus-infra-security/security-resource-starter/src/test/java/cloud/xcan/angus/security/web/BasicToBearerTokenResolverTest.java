package cloud.xcan.angus.security.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for {@link BasicToBearerTokenResolver}.
 *
 * <p>Verifies the artifact-protocol credential bridge: standard Bearer always wins, configured
 * token headers and HTTP Basic password are accepted only when enabled, and the username component
 * of Basic is ignored.</p>
 */
class BasicToBearerTokenResolverTest {

  private BasicToBearerTokenResolver resolver(boolean enabled, boolean acceptBasic,
      List<String> tokenHeaders) {
    BasicAuthBridgeProperties props = new BasicAuthBridgeProperties();
    props.setEnabled(enabled);
    props.setAcceptBasicPassword(acceptBasic);
    props.setTokenHeaders(tokenHeaders);
    var delegate =
        new org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver();
    delegate.setAllowUriQueryParameter(true);
    return new BasicToBearerTokenResolver(delegate, props);
  }

  private static String basic(String user, String password) {
    String raw = user + ":" + password;
    return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void bearerHeader_alwaysWins_evenWhenDisabled() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer real-token");

    assertThat(resolver(false, true, List.of()).resolve(req)).isEqualTo("real-token");
  }

  @Test
  void disabled_basicPassword_isIgnored() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", basic("user", "pat-token"));

    assertThat(resolver(false, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_basicPassword_isUsedAsToken_usernameIgnored() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", basic("anyone", "pat-token"));

    assertThat(resolver(true, true, List.of()).resolve(req)).isEqualTo("pat-token");
  }

  @Test
  void enabled_emptyBasicPassword_returnsNull() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", basic("user", ""));

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_basicWithoutColon_returnsNull() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Basic " + Base64.getEncoder()
        .encodeToString("nocolon".getBytes(StandardCharsets.UTF_8)));

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_malformedBase64_returnsNull() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Basic !!!not-base64!!!");

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_tokenHeader_isUsedAsToken() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-NuGet-ApiKey", "nuget-pat");

    assertThat(resolver(true, true, List.of("X-NuGet-ApiKey")).resolve(req))
        .isEqualTo("nuget-pat");
  }

  @Test
  void enabled_tokenHeaderTakesPrecedenceOverBasic() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-NuGet-ApiKey", "header-pat");
    req.addHeader("Authorization", basic("user", "basic-pat"));

    assertThat(resolver(true, true, List.of("X-NuGet-ApiKey")).resolve(req))
        .isEqualTo("header-pat");
  }

  @Test
  void enabled_acceptBasicPasswordFalse_ignoresBasic() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", basic("user", "pat-token"));

    assertThat(resolver(true, false, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_noCredentials_returnsNull() {
    MockHttpServletRequest req = new MockHttpServletRequest();

    assertThat(resolver(true, true, List.of("X-NuGet-ApiKey")).resolve(req)).isNull();
  }

  @Test
  void enabled_queryParamToken_stillResolvedByDelegate() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addParameter("access_token", "query-token");

    assertThat(resolver(true, true, List.of()).resolve(req)).isEqualTo("query-token");
  }
}
