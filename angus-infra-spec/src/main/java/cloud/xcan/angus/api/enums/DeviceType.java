package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum DeviceType implements Value<String> {
  MOBILE,
  TABLET,
  DESKTOP,
  BOT,
  TV,
  UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
