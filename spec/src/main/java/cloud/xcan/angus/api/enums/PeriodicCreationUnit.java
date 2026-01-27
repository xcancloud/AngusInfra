package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum PeriodicCreationUnit implements Value<String> {
  DAILY, WEEKLY, MONTHLY;

  @Override
  public String getValue() {
    return this.name();
  }
}
