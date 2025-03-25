package cloud.xcan.angus.core.jpa.repository;

import cloud.xcan.angus.core.biz.I18nMessage;
import java.util.List;
import java.util.Set;

public interface I18nMessageJoinRepository<T extends I18nMessage> {

  /**
   * After enabling the cache, use it to query all messages of type
   */
  List<T> findByType(String type);

  /**
   * Use after closing the cache. The query needs to use message
   */
  List<T> findByTypeAndLanguageAndDefaultMessageIn(String type, String language,
      Set<String> defaultMessage);

}
