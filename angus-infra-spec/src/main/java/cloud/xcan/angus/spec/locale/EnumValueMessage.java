package cloud.xcan.angus.spec.locale;

import cloud.xcan.angus.spec.experimental.Message;
import cloud.xcan.angus.spec.experimental.Value;
import cloud.xcan.angus.spec.utils.StringUtils;

public interface EnumValueMessage<V> extends Message, Value<V> {

  @Override
  default String getMessage() {
    return StringUtils.underToUpperCamel(getValue().toString(), true);
  }

  @Override
  default String getMessageKey() {
    return "";
  }

  @Override
  default String getKeyPrefix() {
    return "";
  }
}
