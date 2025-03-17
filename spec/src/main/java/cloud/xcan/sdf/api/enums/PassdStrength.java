package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum PassdStrength implements EnumMessage<String> {
  UNKNOWN, WEAK, MEDIUM, STRONG;

  @Override
  public String getValue() {
    return this.name();
  }

}
