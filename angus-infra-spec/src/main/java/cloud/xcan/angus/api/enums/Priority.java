package cloud.xcan.angus.api.enums;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_PRIORITY;

import cloud.xcan.angus.spec.experimental.Value;


public enum Priority implements Value<String> {
  HIGHEST, HIGH, MEDIUM, LOW, LOWEST;

  public static Priority DEFAULT = MEDIUM;

  @Override
  public String getValue() {
    return this.name();
  }

  public int toExecPriority() {
    if (this.equals(HIGHEST)) {
      return DEFAULT_PRIORITY + 200;
    } else if (this.equals(HIGH)) {
      return DEFAULT_PRIORITY + 100;
    } else if (this.equals(MEDIUM)) {
      return DEFAULT_PRIORITY;
    } else if (this.equals(LOW)) {
      return DEFAULT_PRIORITY - 100;
    } else {
      return DEFAULT_PRIORITY - 200;
    }
  }

}
