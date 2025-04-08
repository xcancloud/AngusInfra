package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum Platform implements EnumMessage<String> {

  XCAN_TP,
  XCAN_OP,
  XCAN_2P,
  XCAN_3RD,
  XCAN_INNER;

  @Override
  public String getValue() {
    return this.name();
  }

}
