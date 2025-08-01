package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum DateUnitType implements EnumMessage<String> {
  DAY,
  MONTH,
  YEAR;

  @Override
  public String getValue() {
    return this.name();
  }
}
