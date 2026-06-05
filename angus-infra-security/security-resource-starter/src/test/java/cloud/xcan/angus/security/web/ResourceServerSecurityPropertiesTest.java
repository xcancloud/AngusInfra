package cloud.xcan.angus.security.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Unit tests for {@link ResourceServerSecurityProperties} and the {@code ?access_token=} URL
 * query-parameter toggle (P2-5).
 *
 * <p>The behavioral cases mirror exactly how {@code OAuth2ResourceServerSecurityAutoConfigurer}
 * applies the property to {@link DefaultBearerTokenResolver#setAllowUriQueryParameter(boolean)},
 * so a regression that drops the wiring is caught here.</p>
 */
class ResourceServerSecurityPropertiesTest {

  @Test
  void allowUriQueryToken_defaultsToFalse_perRfc6750SecureDefault() {
    assertThat(new ResourceServerSecurityProperties().isAllowUriQueryToken()).isFalse();
  }

  @Test
  void allowUriQueryToken_isMutable() {
    ResourceServerSecurityProperties props = new ResourceServerSecurityProperties();
    props.setAllowUriQueryToken(true);
    assertThat(props.isAllowUriQueryToken()).isTrue();
  }

  @Test
  void whenDisabled_uriQueryTokenIsIgnored() {
    DefaultBearerTokenResolver resolver = resolverFor(false);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/raw/repo/a.txt");
    req.addParameter("access_token", "query-token");

    // RFC 6750 §2.3 carrier disabled: the token in the URL must not be accepted.
    assertThat(resolver.resolve(req)).isNull();
  }

  @Test
  void whenEnabled_uriQueryTokenIsAccepted() {
    DefaultBearerTokenResolver resolver = resolverFor(true);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/raw/repo/a.txt");
    req.addParameter("access_token", "query-token");

    assertThat(resolver.resolve(req)).isEqualTo("query-token");
  }

  @Test
  void disablingQueryToken_doesNotAffectAuthorizationBearerHeader() {
    DefaultBearerTokenResolver resolver = resolverFor(false);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/raw/repo/a.txt");
    req.addHeader("Authorization", "Bearer header-token");

    assertThat(resolver.resolve(req)).isEqualTo("header-token");
  }

  /**
   * Builds the resolver the same way the autoconfigurer does: the property value drives
   * {@code setAllowUriQueryParameter}.
   */
  private static DefaultBearerTokenResolver resolverFor(boolean allowUriQueryToken) {
    ResourceServerSecurityProperties props = new ResourceServerSecurityProperties();
    props.setAllowUriQueryToken(allowUriQueryToken);
    DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
    resolver.setAllowUriQueryParameter(props.isAllowUriQueryToken());
    return resolver;
  }
}
