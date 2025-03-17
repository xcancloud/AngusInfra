package cloud.xcan.sdf.validator;

import cloud.xcan.sdf.spec.experimental.Value;

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
