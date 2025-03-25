package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum CalcProgressMethod implements EnumMessage<String> {
  WORKLOAD, NUMBER;

  public boolean isWorkload() {
    return this == WORKLOAD;
  }

  public boolean isNumber() {
    return this == NUMBER;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
