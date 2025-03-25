package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum UseStatus implements EnumMessage<String> {
  IN_USE, NOT_IN_USE;

  @Override
  public String getValue() {
    return this.name();
  }
}
