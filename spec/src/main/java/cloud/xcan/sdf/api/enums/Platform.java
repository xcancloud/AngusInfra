package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum Platform implements EnumMessage<String> {

  XCAN_TP,
  XCAN_OP,
  XCAN_2P,
  XCAN_3RD;

  @Override
  public String getValue() {
    return this.name();
  }

}
