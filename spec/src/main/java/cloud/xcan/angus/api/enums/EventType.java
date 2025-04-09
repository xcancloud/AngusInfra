package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EventType implements EnumMessage<String> {
  BUSINESS(true), SECURITY(true),
  QUOTA(true), SYSTEM(true),
  OPERATION(false), PROTOCOL(true),
  API(false), NOTICE(false), OTHER(false);

  /**
   * Flag to support events based on exception mechanism.
   */
  public final boolean exceptional;

  EventType(boolean exceptional) {
    this.exceptional = exceptional;
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public static EventType from(String type) {
    for (EventType value : values()) {
      if (value.getValue().equalsIgnoreCase(type)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown type: " + type);
  }

  public String getExceptionEventBizKey() {
    return exceptional ? getValue() : null;
  }

  public String getExceptionEventBigBizKey() {
    return exceptional ? getValue() : null;
  }
}
