package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Process-local expireable Caffeine cache for i18n messages.
 *
 * <p>Structure: {@code type → language → (messageKey → I18nMessage)}. No Redis.</p>
 *
 * @author XiaoLong Liu
 */
@Slf4j
public class I18nMessageCache {

  public static final String CACHE_NAME = "I18N_MESSAGE_CACHE";

  private final Cache<String, Map<String, Map<String, I18nMessage>>> cache;
  private final boolean enabled;

  public I18nMessageCache(I18nMessageProperties properties) {
    I18nMessageProperties.Cache cacheProps = properties.getCache();
    this.enabled = cacheProps.isEnabled();
    this.cache = Caffeine.newBuilder()
        .maximumSize(cacheProps.getMaximumSize())
        .expireAfterWrite(cacheProps.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
        .expireAfterAccess(cacheProps.getExpireAfterAccessMinutes(), TimeUnit.MINUTES)
        .build();
  }

  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns language → (messageKey → message) for a type, or {@code null} on miss.
   */
  public Map<String, Map<String, I18nMessage>> get(String type) {
    if (!enabled || isEmpty(type)) {
      return null;
    }
    return cache.getIfPresent(type);
  }

  public Map<String, I18nMessage> getLanguageMap(String type, String language) {
    Map<String, Map<String, I18nMessage>> typeMap = get(type);
    if (isEmpty(typeMap) || isEmpty(language)) {
      return null;
    }
    return typeMap.get(language);
  }

  /**
   * Merge messages into the type cache (does not replace other languages).
   * Passing an empty list still registers the type so repeated DB misses are avoided within TTL.
   */
  public void putAll(String type, List<? extends I18nMessage> messages) {
    if (!enabled || isEmpty(type)) {
      return;
    }
    Map<String, Map<String, I18nMessage>> typeMap = cache.get(type, k -> new HashMap<>());
    synchronized (typeMap) {
      if (isNotEmpty(messages)) {
        for (I18nMessage message : messages) {
          if (message == null || isEmpty(message.getLanguage())) {
            continue;
          }
          String key = resolveKey(message);
          if (isEmpty(key)) {
            continue;
          }
          typeMap.computeIfAbsent(message.getLanguage(), lang -> new HashMap<>())
              .put(key, message);
          // Also index by default_message for legacy lookups within the same language map
          if (isNotEmpty(message.getDefaultMessage())
              && !message.getDefaultMessage().equals(key)) {
            typeMap.get(message.getLanguage()).putIfAbsent(message.getDefaultMessage(), message);
          }
        }
      }
    }
    cache.put(type, typeMap);
    log.debug("Warmed i18n cache for type={}, languages={}", type, typeMap.keySet());
  }

  public void evict(String type) {
    if (isEmpty(type)) {
      return;
    }
    cache.invalidate(type);
    log.debug("Evicted i18n cache type={}", type);
  }

  public void evictAll() {
    cache.invalidateAll();
    log.debug("Evicted all i18n message cache entries");
  }

  /**
   * Snapshot for actuator / diagnostics. Returns an unmodifiable shallow copy.
   */
  public Map<String, Map<String, Map<String, I18nMessage>>> snapshot() {
    return Collections.unmodifiableMap(new HashMap<>(cache.asMap()));
  }

  public long estimatedSize() {
    return cache.estimatedSize();
  }

  private static String resolveKey(I18nMessage message) {
    if (isNotEmpty(message.getMessageKey())) {
      return message.getMessageKey();
    }
    return message.getDefaultMessage();
  }
}
