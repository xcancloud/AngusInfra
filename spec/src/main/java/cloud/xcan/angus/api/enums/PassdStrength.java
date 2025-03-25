package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum PassdStrength implements EnumMessage<String> {
  UNKNOWN, WEAK, MEDIUM, STRONG;

  @Override
  public String getValue() {
    return this.name();
  }

}
