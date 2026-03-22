package cloud.xcan.angus.core.spring.locale;


import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_LOCALE;

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
 * headers
 * <p>
 * Priority order: Query parameters > Cookie > Request headers > Default locale
 */
public class MultiSourceLocaleResolver implements LocaleResolver {

  // Supported query parameter names
  private static final List<String> LANG_PARAM_NAMES = Arrays.asList("lang", "locale", "language");

  // Supported cookie names
  private static final List<String> LANG_COOKIE_NAMES = Arrays.asList("USER_LANG", "APP_LANG");

  // Supported header names
  private static final List<String> LANG_HEADER_NAMES = Arrays.asList("X-Lang", "Accept-Language");

  // Default locale (configurable)
  private Locale defaultLocale = DEFAULT_LOCALE;

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    // 1. Try to get from query parameters
    Optional<String> paramLang = LANG_PARAM_NAMES.stream()
        .map(request::getParameter)
        .filter(StringUtils::hasText)
        .findFirst();

    if (paramLang.isPresent()) {
      return parseLocale(paramLang.get());
    }

    // 2. Try to get from cookies
    Optional<String> cookieLang = LANG_COOKIE_NAMES.stream()
        .map(name -> getCookieValue(request, name))
        .filter(StringUtils::hasText)
        .findFirst();

    if (cookieLang.isPresent()) {
      return parseLocale(cookieLang.get());
    }

    // 3. Try to get from request headers
    Optional<String> headerLang = LANG_HEADER_NAMES.stream()
        .map(request::getHeader)
        .filter(StringUtils::hasText)
        .findFirst();

    // 4. Use default locale
    return headerLang.map(this::parseHeaderLocale).orElseGet(() -> defaultLocale);
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    throw new UnsupportedOperationException(
        "MultiSourceLocaleResolver does not support dynamic locale setting");
  }

  // Get value from cookie
  private String getCookieValue(HttpServletRequest request, String name) {
    return Optional.ofNullable(request.getCookies())
        .flatMap(cookies -> Arrays.stream(cookies)
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .map(Cookie::getValue))
        .orElse(null);
  }

  // Parse standard locale string (e.g., zh-CN, en_US)
  private Locale parseLocale(String localeString) {
    if (localeString.contains("_")) {
      String[] parts = localeString.split("_");
      return new Locale(parts[0], parts[1]);
    } else if (localeString.contains("-")) {
      String[] parts = localeString.split("-");
      return new Locale(parts[0], parts[1]);
    }
    return new Locale(localeString);
  }

  // Special handling for Accept-Language header (e.g., "zh-CN,zh;q=0.9,en;q=0.8")
  private Locale parseHeaderLocale(String headerValue) {
    if (headerValue.contains(",")) {
      // Take the first language option
      String primaryLang = headerValue.split(",")[0].trim();
      return parseLocale(primaryLang);
    }
    return parseLocale(headerValue);
  }

  // Configure default locale
  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }
}
