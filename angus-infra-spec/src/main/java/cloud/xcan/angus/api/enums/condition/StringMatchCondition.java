package cloud.xcan.angus.api.enums.condition;

import cloud.xcan.angus.spec.experimental.Value;

public enum StringMatchCondition implements Value<String> {
  EQUAL,
  NOT_EQUAL,
  IS_EMPTY,
  NOT_EMPTY,
  CONTAIN,
  NOT_CONTAIN,
  REG_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }

}
