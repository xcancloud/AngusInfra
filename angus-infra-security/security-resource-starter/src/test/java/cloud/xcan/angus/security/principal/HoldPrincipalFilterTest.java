package cloud.xcan.angus.security.principal;

import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_FULL_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_GRANT_TYPE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PRINCIPAL;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_SYS_ADMIN;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_USERNAME;
import static cloud.xcan.angus.spec.SpecConstant.LOCALE_EXPLICIT_REQUEST_ATTR;
import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.spec.locale.SdfLocaleHolder;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

/**
 * 方案 A 回归：{@link HoldPrincipalFilter} 对「非 API（permitAll）协议路径」的主体填充行为。
 *
 * <p>背景缺陷：制品协议路径（{@code /raw/**}、{@code /maven/**} 等）走 Spring Security 的
 * {@code .anyRequest().permitAll()}，不在 {@code AUTH_API_MATCHERS} 中。本过滤器原先对这些路径直接
 * {@code chain.doFilter} 放行，从不把 introspection 得到的身份写入 {@link PrincipalContext}。结果：
 * 即便携带有效访问令牌（PAT，grant_type=password）通过了令牌内省，下游
 * {@code RepositoryAccessChecker} 读到的仍是匿名主体 → 上传 / 私有读永远 401。</p>
 *
 * <p>本测试断言修复后的四个关键场景：协议路径携带有效用户令牌→已认证主体可见；匿名→保持匿名且不被
 * 阻断（公开仓库匿名读）；不支持的 grant 类型→best-effort 失败但不阻断；而 {@code /api/**} 路径的
 * 「匿名即 400」严格行为保持不变（无回归）。</p>
 *
 * <p>由于过滤器在 {@code finally} 中 {@link PrincipalContext#remove()}，断言在过滤链执行<b>当时</b>
 * 由记录式 {@link FilterChain} 捕获，而非过滤器返回之后。</p>
 */
class HoldPrincipalFilterTest {

  private final HoldPrincipalFilter filter = new HoldPrincipalFilter(new ObjectMapper());

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
    PrincipalContext.remove();
    LocaleContextHolder.resetLocaleContext();
    SdfLocaleHolder.resetLocaleContext();
  }

  /** 记录式过滤链：捕获下游执行当时的认证态与 userId（过滤器返回后上下文已被清理）。 */
  private static final class RecordingChain implements FilterChain {

    boolean called = false;
    boolean authPassed = false;
    Long userId = null;
    SupportedLanguage defaultLanguage = null;
    Locale sdfLocale = null;

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
        jakarta.servlet.ServletResponse response) {
      this.called = true;
      this.authPassed = PrincipalContext.isAuthPassed();
      this.userId = PrincipalContext.getUserId();
      this.defaultLanguage = PrincipalContext.get().getDefaultLanguage();
      this.sdfLocale = SdfLocaleHolder.getLocale();
    }
  }

  private static BearerTokenAuthentication userBearer(String grantTypeValue, long userId,
      long tenantId) {
    return userBearer(grantTypeValue, userId, tenantId, null);
  }

  private static BearerTokenAuthentication userBearer(String grantTypeValue, long userId,
      long tenantId, String defaultLanguage) {
    Map<String, Object> principalClaim = new HashMap<>();
    principalClaim.put(INTROSPECTION_CLAIM_NAMES_TENANT_ID, tenantId);
    principalClaim.put(INTROSPECTION_CLAIM_NAMES_USERNAME, "lxl");
    principalClaim.put(INTROSPECTION_CLAIM_NAMES_ID, userId);
    principalClaim.put(INTROSPECTION_CLAIM_NAMES_FULL_NAME, "Xiao Long");
    principalClaim.put(INTROSPECTION_CLAIM_NAMES_SYS_ADMIN, false);
    if (defaultLanguage != null) {
      principalClaim.put(INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE, defaultLanguage);
    }

    Map<String, Object> attributes = new HashMap<>();
    attributes.put(INTROSPECTION_CLAIM_NAMES_CLIENT_ID, "test-client");
    attributes.put(INTROSPECTION_CLAIM_NAMES_GRANT_TYPE, grantTypeValue);
    attributes.put(INTROSPECTION_CLAIM_NAMES_PRINCIPAL, principalClaim);

    DefaultOAuth2AuthenticatedPrincipal principal =
        new DefaultOAuth2AuthenticatedPrincipal(attributes,
            AuthorityUtils.createAuthorityList("SCOPE_repo"));
    OAuth2AccessToken token = new OAuth2AccessToken(TokenType.BEARER, "opaque-pat",
        Instant.now(), Instant.now().plusSeconds(3600));
    return new BearerTokenAuthentication(principal, token,
        AuthorityUtils.createAuthorityList("SCOPE_repo"));
  }

  private static MockHttpServletRequest req(String method, String path) {
    // AntPathRequestMatcher derives the request path from servletPath(+pathInfo); MockHttpServletRequest
    // leaves servletPath empty by default, so set it explicitly to mirror a real dispatched request.
    MockHttpServletRequest req = new MockHttpServletRequest(method, path);
    req.setServletPath(path);
    return req;
  }

  @Test
  void protocolPath_withValidUserBearer_hydratesAuthenticatedPrincipal() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("password", 888L, 1L));
    MockHttpServletRequest req = req("PUT", "/raw/raw-public/v1/temp.pdf");
    req.addHeader("Authorization", "Basic dXNlcjpvcGFxdWUtcGF0"); // user:opaque-pat
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.authPassed).isTrue();
    assertThat(chain.userId).isEqualTo(888L);
  }

  @Test
  void protocolPath_anonymous_remainsAnonymous_andProceeds() throws Exception {
    // No authentication in context (anonymous protocol request, e.g. public-repo read).
    MockHttpServletRequest req = req("GET", "/raw/raw-public/v1/temp.pdf");
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.authPassed).isFalse();
  }

  @Test
  void protocolPath_anonymousAuthenticationToken_remainsAnonymous_andProceeds() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
        "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    MockHttpServletRequest req = req("GET", "/maven/repo/com/x/1.0/x.jar");
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.authPassed).isFalse();
  }

  @Test
  void protocolPath_unsupportedGrantType_doesNotBlock_leavesAnonymous() throws Exception {
    // sms_code is neither PASSWORD nor CLIENT_CREDENTIALS → holdAuthPrincipal throws; the
    // best-effort branch must swallow it and proceed without blocking the protocol request.
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("sms_code", 999L, 1L));
    MockHttpServletRequest req = req("PUT", "/npm/repo/-/pkg.tgz");
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.authPassed).isFalse();
  }

  @Test
  void apiPath_anonymous_writes400_andDoesNotProceed() throws Exception {
    // Regression guard: the strict "/api/** anonymous → 400 PRINCIPAL_MISSING" behavior is unchanged.
    SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
        "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    MockHttpServletRequest req = req("GET", "/api/v1/repo/list");
    MockHttpServletResponse resp = new MockHttpServletResponse();
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, resp, chain);

    assertThat(chain.called).isFalse();
    assertThat(resp.getStatus()).isEqualTo(400);
  }

  @Test
  void apiPath_withValidUserBearer_hydratesAuthenticatedPrincipal() throws Exception {
    // Regression guard: the existing matched-path hydration path still works.
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("password", 777L, 2L));
    MockHttpServletRequest req = req("GET", "/api/v1/repo/list");
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.authPassed).isTrue();
    assertThat(chain.userId).isEqualTo(777L);
  }

  @Test
  void apiPath_explicitRequestLanguage_keepsRequestLocaleOverUserClaim() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("password", 1L, 1L, "zh_CN"));
    MockHttpServletRequest req = req("GET", "/api/v1/repo/list");
    req.setAttribute(LOCALE_EXPLICIT_REQUEST_ATTR, Boolean.TRUE);
    PrincipalContext.createIfAbsent().setDefaultLanguage(SupportedLanguage.en);
    SdfLocaleHolder.setLocale(Locale.ENGLISH);
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.defaultLanguage).isEqualTo(SupportedLanguage.en);
    assertThat(SupportedLanguage.safeLanguage(chain.sdfLocale)).isEqualTo(SupportedLanguage.en);
  }

  @Test
  void apiPath_noExplicitLocale_usesUserClaimAndSyncsHolders() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("password", 1L, 1L, "zh_CN"));
    MockHttpServletRequest req = req("GET", "/api/v1/repo/list");
    req.setAttribute(LOCALE_EXPLICIT_REQUEST_ATTR, Boolean.FALSE);
    PrincipalContext.createIfAbsent().setDefaultLanguage(SupportedLanguage.en);
    SdfLocaleHolder.setLocale(Locale.ENGLISH);
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.defaultLanguage).isEqualTo(SupportedLanguage.zh_CN);
    assertThat(SupportedLanguage.safeLanguage(chain.sdfLocale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void apiPath_userClaimEnUs_normalizedWithoutThrowing() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(userBearer("password", 1L, 1L, "en-US"));
    MockHttpServletRequest req = req("GET", "/api/v1/repo/list");
    req.setAttribute(LOCALE_EXPLICIT_REQUEST_ATTR, Boolean.FALSE);
    RecordingChain chain = new RecordingChain();

    filter.doFilter(req, new MockHttpServletResponse(), chain);

    assertThat(chain.called).isTrue();
    assertThat(chain.defaultLanguage).isEqualTo(SupportedLanguage.en);
  }
}
