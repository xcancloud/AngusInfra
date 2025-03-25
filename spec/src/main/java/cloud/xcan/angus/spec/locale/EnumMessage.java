package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.experimental.BizConstant.ENUM_MESSAGE_PREFIX;

import cloud.xcan.angus.spec.utils.StringUtils;

/**
 * @author XiaoLong Liu
 */
public interface EnumMessage<V> extends EnumValueMessage<V> {

  @Override
  default String getMessage() {
    String message = MessageHolder.message(this.getMessageKey());
    return StringUtils.isBlank(message) ? StringUtils.underToUpperCamel(getValue().toString(), true)
        : message;
  }

  @Override
  default String getMessageKey() {
    return getKeyPrefix() + this.getClass().getSimpleName() + "." + this.getValue();
  }

  @Override
  default String getKeyPrefix() {
    return ENUM_MESSAGE_PREFIX;
  }
}
