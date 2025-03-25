package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum PeriodicUnit implements EnumMessage<String> {
  EVERY_MINUTE, HOURLY, DAILY, WEEKLY, MONTHLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
