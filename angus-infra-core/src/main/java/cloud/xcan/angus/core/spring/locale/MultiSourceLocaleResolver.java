package cloud.xcan.angus.core.spring.locale;


import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_LOCALE;
import static cloud.xcan.angus.spec.SpecConstant.LOCALE_EXPLICIT_REQUEST_ATTR;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.LANG;
import static cloud.xcan.angus.spec.http.HttpRequestHeader.Accept_Language;

import cloud.xcan.angus.spec.locale.SupportedLanguage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Multi-source locale resolver - Resolves language from query parameters, cookies, and request
 * headers.
 * <p>
 * Priority: query ({@code lang|locale|language}) &gt; cookie ({@code USER_LANG|APP_LANG}) &gt;
 * header ({@code X-Lang}) &gt; {@code Accept-Language} (RFC 4647 {@code q} weights) &gt; system
 * default ({@link cloud.xcan.angus.spec.SpecConstant#DEFAULT_LOCALE}, derived from
 * {@code BizConstant.DEFAULT_LANGUAGE}).
 * <p>
 * Sets {@link cloud.xcan.angus.spec.SpecConstant#LOCALE_EXPLICIT_REQUEST_ATTR} to indicate whether
 * the locale came from the request (not the system default).
 */
public class MultiSourceLocaleResolver implements LocaleResolver {

  private static final List<String> LANG_PARAM_NAMES = Arrays.asList("lang", "locale", "language");

  private static final List<String> LANG_COOKIE_NAMES = Arrays.asList("USER_LANG", "APP_LANG");

  private Locale defaultLocale = DEFAULT_LOCALE;

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    Optional<String> paramLang = LANG_PARAM_NAMES.stream()
        .map(request::getParameter)
        .filter(StringUtils::hasText)
        .findFirst();
    if (paramLang.isPresent()) {
      return markExplicit(request, SupportedLanguage.of(paramLang.get()).toLocale());
    }

    Optional<String> cookieLang = LANG_COOKIE_NAMES.stream()
        .map(name -> getCookieValue(request, name))
        .filter(StringUtils::hasText)
        .findFirst();
    if (cookieLang.isPresent()) {
      return markExplicit(request, SupportedLanguage.of(cookieLang.get()).toLocale());
    }

    String xLang = request.getHeader(LANG);
    if (StringUtils.hasText(xLang)) {
      return markExplicit(request, SupportedLanguage.of(xLang).toLocale());
    }

    String acceptLanguage = request.getHeader(Accept_Language.getValue());
    if (StringUtils.hasText(acceptLanguage)) {
      return markExplicit(request, SupportedLanguage.lookupFromAcceptLanguage(acceptLanguage));
    }

    request.setAttribute(LOCALE_EXPLICIT_REQUEST_ATTR, Boolean.FALSE);
    return defaultLocale;
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    throw new UnsupportedOperationException(
        "MultiSourceLocaleResolver does not multitenancy dynamic locale setting");
  }

  public static boolean isExplicitLocale(HttpServletRequest request) {
    return Boolean.TRUE.equals(request.getAttribute(LOCALE_EXPLICIT_REQUEST_ATTR));
  }

  private static Locale markExplicit(HttpServletRequest request, Locale locale) {
    request.setAttribute(LOCALE_EXPLICIT_REQUEST_ATTR, Boolean.TRUE);
    return locale;
  }

  private String getCookieValue(HttpServletRequest request, String name) {
    return Optional.ofNullable(request.getCookies())
        .flatMap(cookies -> Arrays.stream(cookies)
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .map(Cookie::getValue))
        .orElse(null);
  }

  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }
}
