package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum NormalStatus implements Value<String> {
  NORMAL, EXCEPTION, UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
