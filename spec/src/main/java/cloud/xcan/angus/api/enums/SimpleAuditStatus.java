package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.locale.EnumMessage;

public enum SimpleAuditStatus implements EnumMessage<String> {
  PENDING,
  PASSED,
  FAILED;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isPassed() {
    return this.equals(PASSED);
  }

  public boolean isFailed() {
    return this.equals(FAILED);
  }
}
