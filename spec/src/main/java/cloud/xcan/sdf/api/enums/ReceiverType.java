package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ReceiverType implements EnumMessage<String> {
  TESTER, EXECUTOR, OWNER, FOLLOWER, APP_ADMIN, OTHER;

  @Override
  public String getValue() {
    return this.name();
  }
}
