package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_LANGUAGE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.experimental.Value;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Optional;

/**
 * Application-supported languages. Accepts BCP-47 / underscore variants and normalizes them.
 *
 * <p><b>Normalization policy</b>
 * <ul>
 *   <li>{@code en}, {@code en-US}, {@code en_US}, {@code en-GB} → {@link #en}</li>
 *   <li>{@code zh}, {@code zh-CN}, {@code zh_CN}, {@code zh-TW}, {@code zh-HK} → {@link #zh_CN}
 *       (product currently ships Simplified Chinese only; Traditional tags fall back to
 *       {@code zh_CN})</li>
 *   <li>Malformed tags ({@code en_}, {@code ;q=0.9}, blank) → {@link #defaultLanguage()}</li>
 *   <li>Unknown languages ({@code fr}, …) → {@link #defaultLanguage()}</li>
 * </ul>
 *
 * @author XiaoLong Liu
 */
public enum SupportedLanguage implements Value<String> {
  en,
  zh_CN;

  /**
   * Locales offered to {@link Locale#lookup(List, List)} for Accept-Language matching.
   */
  private static final List<Locale> LOOKUP_LOCALES = List.of(
      Locale.ENGLISH,
      Locale.US,
      Locale.CHINA,
      Locale.SIMPLIFIED_CHINESE,
      Locale.TRADITIONAL_CHINESE,
      Locale.TAIWAN,
      Locale.forLanguageTag("zh"));

  @Override
  public String getValue() {
    return this.name();
  }

  public Locale toLocale() {
    if (this == SupportedLanguage.en) {
      return Locale.ENGLISH;
    }
    return Locale.CHINA;
  }

  /**
   * HTTP Accept-Language / Content-Language style tag (e.g. {@code en}, {@code zh-CN}).
   */
  public String toLanguageTag() {
    if (this == SupportedLanguage.en) {
      return "en";
    }
    return "zh-CN";
  }

  /**
   * Exact enum-name match only ({@code en}, {@code zh_CN}). Prefer {@link #matches(String)} for
   * BCP-47 inputs.
   */
  public static boolean contain(String value) {
    if (isEmpty(value)) {
      return false;
    }
    for (SupportedLanguage language : SupportedLanguage.values()) {
      if (language.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether {@code value} maps to a supported language <em>without</em> falling back to the system
   * default (e.g. {@code en-US} / {@code zh-TW} → true, {@code fr} → false).
   */
  public static boolean matches(String value) {
    return tryParse(value).isPresent();
  }

  /**
   * System default language from {@link cloud.xcan.angus.spec.experimental.BizConstant#DEFAULT_LANGUAGE}.
   */
  public static SupportedLanguage defaultLanguage() {
    try {
      return SupportedLanguage.valueOf(DEFAULT_LANGUAGE);
    } catch (RuntimeException ex) {
      return en;
    }
  }

  /**
   * {@link #defaultLanguage()} as a {@link Locale} (same as {@code SpecConstant.DEFAULT_LOCALE}).
   */
  public static Locale defaultLocale() {
    return defaultLanguage().toLocale();
  }

  /**
   * Normalize any language tag / enum name to a supported language; unknown / malformed →
   * {@link #defaultLanguage()}.
   */
  public static SupportedLanguage of(String value) {
    return tryParse(value).orElseGet(SupportedLanguage::defaultLanguage);
  }

  /**
   * Parse without default fallback. Empty when the tag is blank, malformed beyond recovery, or an
   * unsupported language family.
   */
  public static Optional<SupportedLanguage> tryParse(String value) {
    if (isEmpty(value)) {
      return Optional.empty();
    }
    String normalized = normalizeTag(value);
    if (isEmpty(normalized)) {
      return Optional.empty();
    }
    for (SupportedLanguage language : values()) {
      if (language.name().equalsIgnoreCase(normalized)) {
        return Optional.of(language);
      }
    }
    String languagePart = languagePart(normalized);
    if (isEmpty(languagePart)) {
      return Optional.empty();
    }
    if ("en".equalsIgnoreCase(languagePart)) {
      return Optional.of(en);
    }
    if ("zh".equalsIgnoreCase(languagePart)) {
      // zh / zh-CN / zh-TW / zh-HK → Simplified Chinese (only Chinese locale we ship)
      return Optional.of(zh_CN);
    }
    return Optional.empty();
  }

  /**
   * Pick the best supported locale from an Accept-Language header (respects {@code q} weights).
   */
  public static Locale lookupFromAcceptLanguage(String acceptLanguage) {
    if (isEmpty(acceptLanguage)) {
      return defaultLanguage().toLocale();
    }
    try {
      List<LanguageRange> ranges = LanguageRange.parse(acceptLanguage.trim());
      Locale matched = Locale.lookup(ranges, LOOKUP_LOCALES);
      if (matched != null) {
        return of(matched.toLanguageTag()).toLocale();
      }
      for (LanguageRange range : ranges) {
        Optional<SupportedLanguage> parsed = tryParse(range.getRange());
        if (parsed.isPresent()) {
          return parsed.get().toLocale();
        }
      }
    } catch (IllegalArgumentException ignored) {
      return of(acceptLanguage).toLocale();
    }
    return defaultLanguage().toLocale();
  }

  public static Locale safeLocale(Locale locale) {
    return safeLanguage(locale).toLocale();
  }

  public static SupportedLanguage safeLanguage(Locale locale) {
    if (locale == null) {
      return defaultLanguage();
    }
    String tag = locale.toLanguageTag();
    if ("und".equalsIgnoreCase(tag)) {
      return of(locale.toString());
    }
    return of(tag);
  }

  public static SupportedLanguage safeLanguage(String language) {
    return of(language);
  }

  private static String languagePart(String normalized) {
    int underscore = normalized.indexOf('_');
    if (underscore < 0) {
      return normalized;
    }
    if (underscore == 0) {
      return "";
    }
    return normalized.substring(0, underscore);
  }

  /**
   * Strip {@code ;q=} / comma list noise, unify separators, drop empty region segments
   * ({@code en_} → {@code en}).
   */
  static String normalizeTag(String value) {
    String s = value.trim();
    int semi = s.indexOf(';');
    if (semi >= 0) {
      s = s.substring(0, semi).trim();
    }
    int comma = s.indexOf(',');
    if (comma >= 0) {
      s = s.substring(0, comma).trim();
    }
    if (isEmpty(s)) {
      return "";
    }
    s = s.replace('-', '_');
    while (s.endsWith("_")) {
      s = s.substring(0, s.length() - 1);
    }
    while (s.startsWith("_")) {
      s = s.substring(1);
    }
    // "en__US" → "en_US"
    while (s.contains("__")) {
      s = s.replace("__", "_");
    }
    return s;
  }
}
