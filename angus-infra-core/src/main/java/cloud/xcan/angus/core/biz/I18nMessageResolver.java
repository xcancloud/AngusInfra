package cloud.xcan.angus.core.biz;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Primary API for resolving DB-backed configuration i18n messages.
 *
 * @author XiaoLong Liu
 * @see DefaultI18nMessageResolver
 */
public interface I18nMessageResolver {

  /**
   * Resolve with the current {@code SdfLocaleHolder} locale; falls back to {@code messageKey}.
   */
  String resolve(String type, String messageKey);

  /**
   * Resolve with the current locale; falls back to {@code fallback} when missing.
   */
  String resolve(String type, String messageKey, String fallback);

  /**
   * Resolve for an explicit locale; falls back to {@code messageKey}.
   */
  String resolve(String type, String messageKey, Locale locale);

  /**
   * Full resolve with locale and fallback.
   */
  String resolve(String type, String messageKey, Locale locale, String fallback);

  /**
   * Batch resolve. Missing keys map to themselves. Uses the type-level Caffeine cache when enabled.
   */
  Map<String, String> resolveBatch(String type, Collection<String> messageKeys, Locale locale);

  /**
   * Batch resolve. When {@code useTypeCache} is {@code false}, loads only the requested keys from
   * DB (no type-level cache warm).
   */
  Map<String, String> resolveBatch(String type, Collection<String> messageKeys, Locale locale,
      boolean useTypeCache);

  /**
   * Invalidate local cache for one type.
   */
  void evict(String type);

  /**
   * Invalidate all local cache entries.
   */
  void evictAll();

  /**
   * Cache snapshot for diagnostics.
   */
  Map<String, Map<String, Map<String, I18nMessage>>> cacheSnapshot();
}
