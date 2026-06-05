package cloud.xcan.angus.security.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * P2-7：制品协议「带凭证认证成功 / 匿名失败」的协议级链路测试。
 *
 * <p>背景：评审发现既有测试只断言 401 挑战头文案，没有验证「客户端按协议约定携带凭证时，凭证是否真的被
 * 还原成 Bearer token 喂进鉴权管线」——正是这个缺口让 P0 缺陷（服务器回 Basic 挑战但后端丢弃
 * {@code Authorization: Basic}，私有仓库经标准 CLI 永远 401）长期未被发现。</p>
 *
 * <p>本测试按 AngusRepo 的生产配置（bridge 开启、接受 Basic 密码、{@code X-NuGet-ApiKey} 头）组装
 * {@link BasicToBearerTokenResolver}，并以各协议真实请求路径为场景，断言鉴权链路的「成功 / 失败」两端：</p>
 * <ul>
 *   <li><b>成功</b>：mvn / npm / pip / helm 的 Basic 密码载体、NuGet 的 {@code X-NuGet-ApiKey} 头、
 *       以及现代客户端的标准 {@code Bearer} 头，都被还原成同一个待校验的 Bearer token；</li>
 *   <li><b>失败</b>：匿名 / 空密码请求解析为 {@code null}（随后由挑战过滤器写 401，见
 *       {@code ProtocolChallengeFilterTest} 对挑战 scheme 的协议级断言）；</li>
 *   <li><b>P2-5</b>：关闭 {@code ?access_token=} 后，协议 GET 路径上的 URL token 不再被接受，
 *       避免泄漏进 access log / 反代日志 / Referer。</li>
 * </ul>
 *
 * <p>本层断言「凭证被正确还原为 Bearer token」；token 的有效性由不透明令牌 introspection
 * （{@code CustomOpaqueTokenIntrospector}）负责，无需真实 AngusGM / 容器。挑战 scheme 与协议对齐
 * （Docker→Bearer，其余→Basic）的部分由 AngusRepo 侧的 {@code ProtocolChallengeFilterTest} 覆盖。</p>
 */
class ArtifactProtocolAuthChainTest {

  /**
   * 按 AngusRepo 生产配置组装桥接解析器；{@code allowUriQueryToken} 对应 autoconfigurer 里由
   * {@link ResourceServerSecurityProperties} 驱动的 {@code setAllowUriQueryParameter}。
   */
  private BearerTokenResolver angusRepoResolver(boolean allowUriQueryToken) {
    BasicAuthBridgeProperties props = new BasicAuthBridgeProperties();
    props.setEnabled(true);
    props.setAcceptBasicPassword(true);
    props.setTokenHeaders(List.of("X-NuGet-ApiKey"));
    DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
    delegate.setAllowUriQueryParameter(allowUriQueryToken);
    return new BasicToBearerTokenResolver(delegate, props);
  }

  private static String basic(String user, String password) {
    String raw = user + ":" + password;
    return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  // ---- 认证成功：各协议凭证载体 → 同一个待校验 Bearer token ----

  @Test
  void maven_basicPasswordCarrier_resolvesToken_usernameIgnored() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/maven/repo/com/x/1.0/x.jar");
    req.addHeader("Authorization", basic("any-user", "pat-token"));

    assertThat(angusRepoResolver(false).resolve(req)).isEqualTo("pat-token");
  }

  @Test
  void npm_tokenSentinelBasicPassword_resolvesToken() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/npm/repo/lodash");
    // npm legacy login 把 token 写成 Basic 密码（用户名常为 __token__，此处被忽略）。
    req.addHeader("Authorization", basic("__token__", "npm-pat"));

    assertThat(angusRepoResolver(false).resolve(req)).isEqualTo("npm-pat");
  }

  @Test
  void nuget_apiKeyHeader_resolvesToken() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/nuget/repo/v3/index.json");
    req.addHeader("X-NuGet-ApiKey", "nuget-api-key");

    assertThat(angusRepoResolver(false).resolve(req)).isEqualTo("nuget-api-key");
  }

  @Test
  void modernClient_standardBearerHeader_stillWins() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/helm/repo/index.yaml");
    req.addHeader("Authorization", "Bearer real-bearer");

    assertThat(angusRepoResolver(false).resolve(req)).isEqualTo("real-bearer");
  }

  // ---- 认证失败：匿名 / 空密码 → null（随后触发挑战）----

  @Test
  void anonymousRequest_resolvesNull() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/maven/repo/com/x/1.0/x.jar");

    assertThat(angusRepoResolver(false).resolve(req)).isNull();
  }

  @Test
  void blankBasicPassword_resolvesNull() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/pypi/repo/simple/");
    req.addHeader("Authorization", basic("user", ""));

    assertThat(angusRepoResolver(false).resolve(req)).isNull();
  }

  // ---- P2-5：?access_token= 收敛 ----

  @Test
  void uriQueryToken_onProtocolPath_isIgnored_whenDisabled() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/raw/repo/file.bin");
    req.addParameter("access_token", "leaky-token");

    assertThat(angusRepoResolver(false).resolve(req)).isNull();
  }

  @Test
  void uriQueryToken_onProtocolPath_isAccepted_whenEnabled() {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/raw/repo/file.bin");
    req.addParameter("access_token", "leaky-token");

    assertThat(angusRepoResolver(true).resolve(req)).isEqualTo("leaky-token");
  }
}
