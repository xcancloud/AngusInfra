package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.Value;


public enum NumberMatchCondition implements Value<String> {
  EQUAL,
  NOT_EQUAL,
  IS_EMPTY,
  NOT_EMPTY,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL,
  REG_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }

}
