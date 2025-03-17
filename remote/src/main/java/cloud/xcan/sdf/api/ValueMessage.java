package cloud.xcan.sdf.api;

import static cloud.xcan.sdf.spec.experimental.BizConstant.VALUE_MESSAGE_PREFIX;

import cloud.xcan.sdf.spec.experimental.Message;
import cloud.xcan.sdf.spec.experimental.Value;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import org.apache.commons.lang3.StringUtils;

public interface ValueMessage<V> extends Message, Value<V> {

  @Override
  default String getMessage() {
    String message = MessageHolder.message(getMessageKey());
    return StringUtils.isBlank(message) ? String.valueOf(this.getValue()) : message;
  }

  @Override
  default String getMessageKey() {
    return VALUE_MESSAGE_PREFIX + this.getClass().getSimpleName() + "." + this.getValue();
  }

}
