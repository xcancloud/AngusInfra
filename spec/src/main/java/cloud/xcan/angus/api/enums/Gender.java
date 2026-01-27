package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum Gender implements Value<String> {
  MALE,
  FEMALE,
  UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
