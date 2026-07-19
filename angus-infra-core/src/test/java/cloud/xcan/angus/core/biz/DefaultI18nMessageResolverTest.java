package cloud.xcan.angus.core.biz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultI18nMessageResolverTest {

  private I18nMessageJoinRepository<I18nMessage> repository;
  private I18nMessageProperties properties;
  private I18nMessageCache cache;
  private DefaultI18nMessageResolver resolver;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    repository = mock(I18nMessageJoinRepository.class);
    properties = new I18nMessageProperties();
    properties.setDefaultLocale("zh_CN");
    cache = new I18nMessageCache(properties);
    resolver = new DefaultI18nMessageResolver(repository, cache, properties);
  }

  @Test
  void resolve_byMessageKey_fromCacheWarm() {
    I18nMessage en = message("ROLE", "en", "ROLE_ADMIN", "管理员", "Administrator");
    I18nMessage zh = message("ROLE", "zh_CN", "ROLE_ADMIN", "管理员", "管理员");
    when(repository.findByType("ROLE")).thenReturn(List.of(en, zh));

    String resolved = resolver.resolve("ROLE", "ROLE_ADMIN", Locale.ENGLISH, "ROLE_ADMIN");
    assertEquals("Administrator", resolved);

    // Second call should not hit DB again for type warm
    resolver.resolve("ROLE", "ROLE_ADMIN", Locale.ENGLISH, "ROLE_ADMIN");
    verify(repository, times(1)).findByType("ROLE");
  }

  @Test
  void resolve_fallsBackToDefaultLocale() {
    I18nMessage zh = message("ROLE", "zh_CN", "ROLE_ADMIN", "管理员", "管理员");
    when(repository.findByType("ROLE")).thenReturn(List.of(zh));

    // French not present → fall back to zh_CN
    String resolved = resolver.resolve("ROLE", "ROLE_ADMIN", Locale.FRENCH, "ROLE_ADMIN");
    assertEquals("管理员", resolved);
  }

  @Test
  void resolve_withoutTypeCache_queriesByKey() {
    I18nMessage en = message("ROLE", "en", "ROLE_USER", "用户", "User");
    when(repository.findByTypeAndLanguageAndMessageKeyIn(eq("ROLE"), eq("en"), any()))
        .thenReturn(List.of(en));

    Map<String, String> batch = resolver.resolveBatch("ROLE", Set.of("ROLE_USER"),
        Locale.ENGLISH, false);
    assertEquals("User", batch.get("ROLE_USER"));
    verify(repository, times(0)).findByType(any());
  }

  @Test
  void resolve_missing_returnsFallback() {
    when(repository.findByType("ROLE")).thenReturn(List.of());
    String resolved = resolver.resolve("ROLE", "MISSING", Locale.ENGLISH, "fallback");
    assertEquals("fallback", resolved);
  }

  @Test
  void localeCandidates_order() {
    List<String> candidates = resolver.localeCandidates(Locale.SIMPLIFIED_CHINESE);
    assertEquals("zh_CN", candidates.get(0));
    assertTrue(candidates.contains("zh"));
    assertTrue(candidates.contains("zh_CN"));
  }

  @Test
  void toLanguageTag_mapsChinaAndEnglish() {
    assertEquals("zh_CN", DefaultI18nMessageResolver.toLanguageTag(Locale.CHINA));
    assertEquals("zh_CN", DefaultI18nMessageResolver.toLanguageTag(Locale.SIMPLIFIED_CHINESE));
    assertEquals("zh_CN", DefaultI18nMessageResolver.toLanguageTag(Locale.TAIWAN));
    assertEquals("zh_CN", DefaultI18nMessageResolver.toLanguageTag(Locale.forLanguageTag("zh-HK")));
    assertEquals("en", DefaultI18nMessageResolver.toLanguageTag(Locale.ENGLISH));
    assertEquals("en", DefaultI18nMessageResolver.toLanguageTag(Locale.US));
  }

  @Test
  void localeCandidates_zhTw_usesZhCnThenZhThenDefault() {
    List<String> candidates = resolver.localeCandidates(Locale.TAIWAN);
    assertEquals("zh_CN", candidates.get(0));
    assertEquals("zh", candidates.get(1));
    assertTrue(candidates.contains(properties.getDefaultLocale()));
  }

  @Test
  void evict_clearsTypeCache() {
    I18nMessage en = message("ROLE", "en", "ROLE_ADMIN", "管理员", "Administrator");
    when(repository.findByType("ROLE")).thenReturn(List.of(en));
    resolver.resolve("ROLE", "ROLE_ADMIN", Locale.ENGLISH);
    resolver.evict("ROLE");
    resolver.resolve("ROLE", "ROLE_ADMIN", Locale.ENGLISH);
    verify(repository, times(2)).findByType("ROLE");
  }

  private static I18nMessage message(String type, String language, String key,
      String defaultMessage, String i18nMessage) {
    return new I18nMessage() {
      @Override
      public String getType() {
        return type;
      }

      @Override
      public String getLanguage() {
        return language;
      }

      @Override
      public String getMessageKey() {
        return key;
      }

      @Override
      public String getDefaultMessage() {
        return defaultMessage;
      }

      @Override
      public String getI18nMessage() {
        return i18nMessage;
      }
    };
  }
}
