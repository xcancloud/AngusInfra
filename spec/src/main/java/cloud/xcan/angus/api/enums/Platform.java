package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum Platform implements Value<String> {

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
