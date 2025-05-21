package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.locale.EnumValueMessage;

public enum MessageType implements EnumValueMessage<String> {
  CHAT, REPLY, JOIN, LEAVE, NOTICE;

  @Override
  public String getValue() {
    return this.name();
  }
}
