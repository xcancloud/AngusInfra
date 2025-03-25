package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.locale.EnumValueMessage;

public enum SupportedDbType implements EnumValueMessage<String> {
  MYSQL, POSTGRES;

  @Override
  public String getValue() {
    return this.name();
  }
}
