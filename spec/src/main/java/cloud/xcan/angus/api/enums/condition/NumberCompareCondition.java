package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.Value;


public enum NumberCompareCondition implements Value<String> {
  EQUAL,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL;

  @Override
  public String getValue() {
    return this.name();
  }

}
