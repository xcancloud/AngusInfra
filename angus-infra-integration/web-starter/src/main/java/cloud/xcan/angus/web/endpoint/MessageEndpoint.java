package cloud.xcan.angus.web.endpoint;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.I18nMessage;
import cloud.xcan.angus.core.biz.I18nMessageResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * Actuator endpoint for inspecting and clearing the local i18n message cache.
 *
 * @author XiaoLong Liu
 */
@Endpoint(id = "messages")
public class MessageEndpoint {

  private final I18nMessageResolver i18nMessageResolver;

  public MessageEndpoint(I18nMessageResolver i18nMessageResolver) {
    this.i18nMessageResolver = i18nMessageResolver;
  }

  @WriteOperation
  public void clearCache() {
    i18nMessageResolver.evictAll();
  }

  @ReadOperation
  public Map<String, Map<String, Map<String, I18nMessage>>> readCache(String type) {
    Map<String, Map<String, Map<String, I18nMessage>>> cachedMessageMap
        = i18nMessageResolver.cacheSnapshot();
    if (isEmpty(cachedMessageMap)) {
      return Collections.emptyMap();
    }
    if (isNotEmpty(type)) {
      Map<String, Map<String, Map<String, I18nMessage>>> typeCachedMessageMap = new HashMap<>();
      typeCachedMessageMap.put(type, cachedMessageMap.get(type));
      return typeCachedMessageMap;
    }
    return cachedMessageMap;
  }

}
