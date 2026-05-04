package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum PasswordStrength implements Value<String> {
  UNKNOWN, WEAK, MEDIUM, STRONG;

  @Override
  public String getValue() {
    return this.name();
  }

}
