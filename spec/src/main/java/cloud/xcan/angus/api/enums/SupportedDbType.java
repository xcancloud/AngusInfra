package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum SupportedDbType implements Value<String> {
  MYSQL, POSTGRES;

  @Override
  public String getValue() {
    return this.name();
  }
}
