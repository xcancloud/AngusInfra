package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AuthObjectType implements EnumMessage<String> {
  USER, DEPT, GROUP;

  @Override
  public String getValue() {
    return this.name();
  }
}
