package cloud.xcan.angus.validator;

import cloud.xcan.angus.spec.experimental.Value;

public enum PassdCharType implements Value<String> {
  UPPER_CASE,
  LOWER_CASE,
  DIGITS,
  SPECIAL_CHA;

  @Override
  public String getValue() {
    return this.name();
  }
}
