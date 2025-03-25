package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ReceiverType implements EnumMessage<String> {
  TESTER, EXECUTOR, OWNER, FOLLOWER, APP_ADMIN, OTHER;

  @Override
  public String getValue() {
    return this.name();
  }
}
