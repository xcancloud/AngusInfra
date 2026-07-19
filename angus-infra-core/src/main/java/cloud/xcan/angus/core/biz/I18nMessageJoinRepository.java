package cloud.xcan.angus.core.biz;

import java.util.Collection;
import java.util.List;

/**
 * Persistence port for config i18n messages. Implemented by application Spring Data repositories.
 *
 * @author XiaoLong Liu
 */
public interface I18nMessageJoinRepository<T extends I18nMessage> {

  /**
   * Load all messages of a type (used to warm the type-level Caffeine cache).
   */
  List<T> findByType(String type);

  /**
   * Load messages by type + language + stable keys (cache disabled / targeted load).
   */
  List<T> findByTypeAndLanguageAndMessageKeyIn(String type, String language,
      Collection<String> messageKeys);

  /**
   * Legacy lookup by default display text. Prefer
   * {@link #findByTypeAndLanguageAndMessageKeyIn(String, String, Collection)}.
   */
  List<T> findByTypeAndLanguageAndDefaultMessageIn(String type, String language,
      Collection<String> defaultMessages);

}
