package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum NoticeType implements EnumMessage<String> {
  SMS, EMAIL, IN_SITE;

  @Override
  public String getValue() {
    return this.name();
  }
}
