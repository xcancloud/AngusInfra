package cloud.xcan.angus.remote;

import static cloud.xcan.angus.spec.experimental.BizConstant.VALUE_MESSAGE_PREFIX;

import cloud.xcan.angus.spec.experimental.Message;
import cloud.xcan.angus.spec.experimental.Value;
import cloud.xcan.angus.spec.locale.MessageHolder;
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
