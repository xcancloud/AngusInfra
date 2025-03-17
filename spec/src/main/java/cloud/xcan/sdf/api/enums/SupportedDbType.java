package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.locale.EnumValueMessage;

public enum SupportedDbType implements EnumValueMessage<String> {
  MYSQL, POSTGRES;

  @Override
  public String getValue() {
    return this.name();
  }
}
