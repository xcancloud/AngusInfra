package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum MessageType implements Value<String> {
  CHAT, REPLY, JOIN, LEAVE, NOTICE, CLOSE;

  @Override
  public String getValue() {
    return this.name();
  }
}
