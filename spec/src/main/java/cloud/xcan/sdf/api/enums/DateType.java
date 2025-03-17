package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum DateType implements EnumMessage<String> {
  DAY,
  MONTH,
  YEAR;

  @Override
  public String getValue() {
    return this.name();
  }
}