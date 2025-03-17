package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AuthObjectType implements EnumMessage<String> {
  USER, DEPT, GROUP;

  @Override
  public String getValue() {
    return this.name();
  }
}
