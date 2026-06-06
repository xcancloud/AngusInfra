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

  private BasicToBearerTokenResolver resolver(boolean enabled, boolean acceptBasic,
      List<String> tokenHeaders, List<String> excludePaths) {
    BasicAuthBridgeProperties props = new BasicAuthBridgeProperties();
    props.setEnabled(enabled);
    props.setAcceptBasicPassword(acceptBasic);
    props.setTokenHeaders(tokenHeaders);
    props.setExcludePaths(excludePaths);
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
    // DefaultBearerTokenResolver 仅对 GET（或表单 POST）解析 ?access_token=，故显式用 GET。
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
    req.addParameter("access_token", "query-token");

    assertThat(resolver(true, true, List.of()).resolve(req)).isEqualTo("query-token");
  }

  @Test
  void enabled_npmLoginPath_basicPasswordNotBridged() {
    // npm login（legacy）以 Basic 携带真实账号密码到登录端点；桥接会把密码当 token 引入校验，
    // 导致 introspection 报 invalid_token。登录端点必须豁免桥接，让凭证抵达 facade 自行换票。
    MockHttpServletRequest req =
        new MockHttpServletRequest("PUT", "/npm/my-repo/-/user/org.couchdb.user:lxl");
    req.addHeader("Authorization", basic("lxl", "real-password"));

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_dockerTokenPath_basicPasswordNotBridged() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v2/token");
    req.addHeader("Authorization", basic("lxl", "real-password"));

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_excludedPath_underContextPath_stillExcluded() {
    MockHttpServletRequest req =
        new MockHttpServletRequest("GET", "/repo/ctx/v2/token");
    req.addHeader("Authorization", basic("lxl", "real-password"));

    assertThat(resolver(true, true, List.of()).resolve(req)).isNull();
  }

  @Test
  void enabled_excludedPath_tokenHeaderAlsoSkipped() {
    MockHttpServletRequest req =
        new MockHttpServletRequest("PUT", "/npm/my-repo/-/user/org.couchdb.user:lxl");
    req.addHeader("X-NuGet-ApiKey", "header-pat");

    assertThat(resolver(true, true, List.of("X-NuGet-ApiKey")).resolve(req)).isNull();
  }

  @Test
  void enabled_nonExcludedArtifactPath_basicPasswordStillBridged() {
    // 普通制品请求（非登录端点）仍走桥接：Basic 密码即 token。
    MockHttpServletRequest req =
        new MockHttpServletRequest("GET", "/npm/my-repo/lodash");
    req.addHeader("Authorization", basic("anyone", "pat-token"));

    assertThat(resolver(true, true, List.of()).resolve(req)).isEqualTo("pat-token");
  }

  @Test
  void enabled_excludedLoginPath_genuineBearerStillWins() {
    // 登录端点豁免桥接，但真实 Bearer 仍由 delegate 解析（如 npm logout 携带 Bearer）。
    MockHttpServletRequest req =
        new MockHttpServletRequest("DELETE", "/npm/my-repo/-/user/token/abc");
    req.addHeader("Authorization", "Bearer real-token");

    assertThat(resolver(true, true, List.of()).resolve(req)).isEqualTo("real-token");
  }

  @Test
  void enabled_emptyExcludePaths_noPathIsExcluded() {
    MockHttpServletRequest req =
        new MockHttpServletRequest("GET", "/v2/token");
    req.addHeader("Authorization", basic("lxl", "pat-token"));

    assertThat(resolver(true, true, List.of(), List.of()).resolve(req)).isEqualTo("pat-token");
  }
}
