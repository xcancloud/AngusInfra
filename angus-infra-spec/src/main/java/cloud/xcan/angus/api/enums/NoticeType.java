package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum NoticeType implements Value<String> {
  SMS, EMAIL, IN_SITE;

  @Override
  public String getValue() {
    return this.name();
  }
}
