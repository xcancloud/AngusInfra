package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum UseStatus implements EnumMessage<String> {
  IN_USE, NOT_IN_USE;

  @Override
  public String getValue() {
    return this.name();
  }
}
