package cloud.xcan.angus.core.spring.locale;

import static cloud.xcan.angus.spec.SpecConstant.LOCALE_EXPLICIT_REQUEST_ATTR;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.LANG;
import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.spec.locale.SupportedLanguage;
import jakarta.servlet.http.Cookie;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class MultiSourceLocaleResolverTest {

  private MultiSourceLocaleResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new MultiSourceLocaleResolver();
  }

  @Test
  void acceptLanguage_enUs_resolvesEnglishAndMarksExplicit() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Accept-Language", "en-US,en;q=0.9");

    Locale locale = resolver.resolveLocale(request);

    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.en);
    assertThat(request.getAttribute(LOCALE_EXPLICIT_REQUEST_ATTR)).isEqualTo(Boolean.TRUE);
    assertThat(MultiSourceLocaleResolver.isExplicitLocale(request)).isTrue();
  }

  @Test
  void acceptLanguage_respectsQWeight() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Accept-Language", "en;q=0.5, zh-CN;q=0.9");

    Locale locale = resolver.resolveLocale(request);

    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void xLang_overridesAcceptLanguage() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(LANG, "zh_CN");
    request.addHeader("Accept-Language", "en-US");

    Locale locale = resolver.resolveLocale(request);

    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void queryParam_highestPriority() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("lang", "en");
    request.addHeader(LANG, "zh_CN");

    Locale locale = resolver.resolveLocale(request);

    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.en);
  }

  @Test
  void cookie_usedWhenNoQuery() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("USER_LANG", "zh_CN"));

    Locale locale = resolver.resolveLocale(request);

    assertThat(SupportedLanguage.safeLanguage(locale)).isEqualTo(SupportedLanguage.zh_CN);
  }

  @Test
  void noSource_usesDefaultAndNotExplicit() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    Locale locale = resolver.resolveLocale(request);

    assertThat(locale).isEqualTo(Locale.ENGLISH);
    assertThat(request.getAttribute(LOCALE_EXPLICIT_REQUEST_ATTR)).isEqualTo(Boolean.FALSE);
    assertThat(MultiSourceLocaleResolver.isExplicitLocale(request)).isFalse();
  }
}
