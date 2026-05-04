package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum CalcProgressMethod implements Value<String> {
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
