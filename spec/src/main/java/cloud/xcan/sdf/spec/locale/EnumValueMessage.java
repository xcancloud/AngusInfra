package cloud.xcan.sdf.spec.locale;

import cloud.xcan.sdf.spec.experimental.Message;
import cloud.xcan.sdf.spec.experimental.Value;
import cloud.xcan.sdf.spec.utils.StringUtils;

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
