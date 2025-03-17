package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum PeriodicUnit implements EnumMessage<String> {
  EVERY_MINUTE, HOURLY, DAILY, WEEKLY, MONTHLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
