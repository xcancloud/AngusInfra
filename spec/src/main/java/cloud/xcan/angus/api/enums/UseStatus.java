package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum UseStatus implements Value<String> {
  IN_USE, NOT_IN_USE;

  @Override
  public String getValue() {
    return this.name();
  }
}
