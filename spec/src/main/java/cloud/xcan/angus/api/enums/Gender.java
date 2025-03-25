package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum Gender implements EnumMessage<String> {
  MALE,
  FEMALE,
  UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
