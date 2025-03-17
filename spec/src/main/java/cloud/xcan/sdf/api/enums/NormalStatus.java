package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum NormalStatus implements EnumMessage<String> {
  NORMAL, EXCEPTION, UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
