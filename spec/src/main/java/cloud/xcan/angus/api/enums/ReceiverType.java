package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ReceiverType implements Value<String> {
  TESTER, EXECUTOR, OWNER, FOLLOWER, APP_ADMIN, OTHER;

  @Override
  public String getValue() {
    return this.name();
  }
}
