package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum EventType implements EnumMessage<String> {
  BUSINESS(true), SECURITY(true),
  QUOTA(true), SYSTEM(true),
  OPERATION(false), PROTOCOL(true),
  API(false), NOTICE(false), OTHER(false);

  /**
   * Flag to support events based on exception mechanism.
   */
  public boolean exceptional;

  EventType(boolean exceptional) {
    this.exceptional = exceptional;
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public String getExceptionEventBizKey() {
    return exceptional ? getValue() : null;
  }

  public String getExceptionEventBigBizKey() {
    return exceptional ? getValue() : null;
  }

}
