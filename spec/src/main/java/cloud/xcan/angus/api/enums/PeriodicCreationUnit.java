package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum PeriodicCreationUnit implements EnumMessage<String> {
  DAILY, WEEKLY, MONTHLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
