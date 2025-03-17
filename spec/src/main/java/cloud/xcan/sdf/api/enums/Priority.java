package cloud.xcan.sdf.api.enums;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_PRIORITY;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import java.util.Locale;


@EndpointRegister
public enum Priority implements EnumMessage<String> {
  HIGHEST, HIGH, MEDIUM, LOW, LOWEST;

  public static Priority DEFAULT = MEDIUM;

  @Override
  public String getValue() {
    return this.name();
  }

  public int toExecPriority() {
    if (this.equals(HIGHEST)) {
      return DEFAULT_PRIORITY + 200;
    } else if (this.equals(HIGH)) {
      return DEFAULT_PRIORITY + 100;
    } else if (this.equals(MEDIUM)) {
      return DEFAULT_PRIORITY;
    } else if (this.equals(LOW)) {
      return DEFAULT_PRIORITY - 100;
    } else {
      return DEFAULT_PRIORITY - 200;
    }
  }

  public static Priority ofMessage(String taskTypeMassage, Locale locale) {
    for (Priority value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(taskTypeMassage.trim())) {
        return value;
      }
    }
    return null;
  }
}
