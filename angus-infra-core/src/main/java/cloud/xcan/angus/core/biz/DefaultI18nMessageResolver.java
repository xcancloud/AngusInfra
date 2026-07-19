package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Default resolver: Caffeine (TTL) → DB, with locale fallback chain.
 *
 * @author XiaoLong Liu
 */
@Slf4j
public class DefaultI18nMessageResolver implements I18nMessageResolver {

  private final I18nMessageJoinRepository<? extends I18nMessage> messageRepository;
  private final I18nMessageCache messageCache;
  private final I18nMessageProperties properties;

  public DefaultI18nMessageResolver(
      I18nMessageJoinRepository<? extends I18nMessage> messageRepository,
      I18nMessageCache messageCache,
      I18nMessageProperties properties) {
    this.messageRepository = messageRepository;
    this.messageCache = messageCache;
    this.properties = properties;
  }

  @Override
  public String resolve(String type, String messageKey) {
    return resolve(type, messageKey, getLocale(), messageKey);
  }

  @Override
  public String resolve(String type, String messageKey, String fallback) {
    return resolve(type, messageKey, getLocale(), fallback);
  }

  @Override
  public String resolve(String type, String messageKey, Locale locale) {
    return resolve(type, messageKey, locale, messageKey);
  }

  @Override
  public String resolve(String type, String messageKey, Locale locale, String fallback) {
    return resolveInternal(type, messageKey, locale, fallback, true);
  }

  @Override
  public Map<String, String> resolveBatch(String type, Collection<String> messageKeys,
      Locale locale) {
    return resolveBatch(type, messageKeys, locale, true);
  }

  @Override
  public Map<String, String> resolveBatch(String type, Collection<String> messageKeys,
      Locale locale, boolean useTypeCache) {
    if (isEmpty(type) || isEmpty(messageKeys)) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<>(messageKeys.size());
    for (String key : messageKeys) {
      if (isEmpty(key)) {
        continue;
      }
      result.put(key, resolveInternal(type, key, locale, key, useTypeCache));
    }
    return result;
  }

  private String resolveInternal(String type, String messageKey, Locale locale, String fallback,
      boolean useTypeCache) {
    if (isEmpty(type) || isEmpty(messageKey)) {
      return fallback;
    }
    if (messageRepository == null) {
      log.warn("I18nMessageJoinRepository is not available, return fallback for type={}, key={}",
          type, messageKey);
      return fallback;
    }

    for (String language : localeCandidates(locale)) {
      I18nMessage hit = findMessage(type, language, messageKey, useTypeCache);
      if (hit != null && isNotEmpty(hit.getI18nMessage())) {
        return hit.getI18nMessage();
      }
    }
    return isNotEmpty(fallback) ? fallback : messageKey;
  }

  @Override
  public void evict(String type) {
    messageCache.evict(type);
  }

  @Override
  public void evictAll() {
    messageCache.evictAll();
  }

  @Override
  public Map<String, Map<String, Map<String, I18nMessage>>> cacheSnapshot() {
    return messageCache.snapshot();
  }

  /**
   * Find one message; optionally warm type cache on miss when cache is enabled.
   */
  I18nMessage findMessage(String type, String language, String messageKey, boolean useCache) {
    if (useCache && messageCache.isEnabled()) {
      Map<String, I18nMessage> languageMap = messageCache.getLanguageMap(type, language);
      if (languageMap == null) {
        warmTypeCache(type);
        languageMap = messageCache.getLanguageMap(type, language);
      }
      if (languageMap != null) {
        I18nMessage cached = languageMap.get(messageKey);
        if (cached != null) {
          return cached;
        }
      }
      return null;
    }
    return loadOneFromDb(type, language, messageKey);
  }

  private void warmTypeCache(String type) {
    List<? extends I18nMessage> messages = messageRepository.findByType(type);
    messageCache.putAll(type, isEmpty(messages) ? Collections.emptyList() : messages);
  }

  private I18nMessage loadOneFromDb(String type, String language, String messageKey) {
    List<? extends I18nMessage> byKey = messageRepository
        .findByTypeAndLanguageAndMessageKeyIn(type, language, Set.of(messageKey));
    if (isNotEmpty(byKey)) {
      return byKey.get(0);
    }
    List<? extends I18nMessage> byDefault = messageRepository
        .findByTypeAndLanguageAndDefaultMessageIn(type, language, Set.of(messageKey));
    if (isNotEmpty(byDefault)) {
      return byDefault.get(0);
    }
    return null;
  }

  /**
   * Exact locale tag → language → configured default locale.
   */
  List<String> localeCandidates(Locale locale) {
    LinkedHashSet<String> candidates = new LinkedHashSet<>();
    if (locale != null) {
      String tag = toLanguageTag(locale);
      if (isNotEmpty(tag)) {
        candidates.add(tag);
      }
      if (isNotEmpty(locale.getLanguage())) {
        candidates.add(locale.getLanguage());
      }
    }
    String defaultLocale = properties.getDefaultLocale();
    if (isNotEmpty(defaultLocale)) {
      candidates.add(defaultLocale);
    }
    return new ArrayList<>(candidates);
  }

  /**
   * Normalize to SupportedLanguage-style tags: {@code zh_CN}, {@code en}.
   */
  static String toLanguageTag(Locale locale) {
    if (locale == null) {
      return null;
    }
    // Locale.CHINA / SIMPLIFIED_CHINESE → zh_CN
    if (Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.CHINA.equals(locale)
        || ("zh".equals(locale.getLanguage()) && "CN".equalsIgnoreCase(locale.getCountry()))) {
      return "zh_CN";
    }
    if (Locale.ENGLISH.equals(locale) || "en".equalsIgnoreCase(locale.getLanguage())) {
      // Prefer plain "en" to match SupportedLanguage.en
      if (isEmpty(locale.getCountry()) || "US".equalsIgnoreCase(locale.getCountry())
          || "GB".equalsIgnoreCase(locale.getCountry())) {
        return "en";
      }
    }
    String language = locale.getLanguage();
    String country = locale.getCountry();
    if (isNotEmpty(country)) {
      return language + "_" + country;
    }
    return language;
  }
}
