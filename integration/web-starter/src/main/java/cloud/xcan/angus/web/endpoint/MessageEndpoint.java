package cloud.xcan.angus.web.endpoint;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.biz.I18nMessage;
import cloud.xcan.angus.core.biz.I18nMessageAspect;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

@Endpoint(id = "messages")
public class MessageEndpoint {

  @Autowired(required = false)
  private I18nMessageAspect i18nMessageAspect;

  private final String authKey = new Str0(
      new long[]{0x2E5F5EE44DE0B1AEL, 0x7900D1B0B65C00B8L, 0x47C923D6D8D5E1CAL})
      .toString() /* => "sdf@message" */;

  @WriteOperation
  public void clearCache(@Selector String auth) {
    if (Objects.isNull(i18nMessageAspect) || !authKey.equals(auth)) {
      return;
    }
    i18nMessageAspect.clearCache();
  }

  @ReadOperation
  public Map<String, Map<String, Map<String, I18nMessage>>> readCache(@Selector String source,
      String auth, String type) {
    if (StringUtils.isEmpty(source) || Objects.isNull(i18nMessageAspect) || !authKey.equals(auth)) {
      return Collections.emptyMap();
    }
    Map<String, Map<String, Map<String, I18nMessage>>> cachedMessageMap = i18nMessageAspect
        .getTypeLanguageMessagesMap();
    if (ObjectUtils.isEmpty(cachedMessageMap)) {
      return Collections.emptyMap();
    }
    if (ObjectUtils.isNotEmpty(type)) {
      Map<String, Map<String, Map<String, I18nMessage>>> typeCachedMessageMap = new HashMap<>();
      typeCachedMessageMap.put(type, cachedMessageMap.get(type));
      return typeCachedMessageMap;
    }
    return cachedMessageMap;
  }

}
