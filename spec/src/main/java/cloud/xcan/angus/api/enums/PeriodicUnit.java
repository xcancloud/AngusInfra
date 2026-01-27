package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum PeriodicUnit implements Value<String> {
  EVERY_MINUTE, HOURLY, DAILY, WEEKLY, MONTHLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
