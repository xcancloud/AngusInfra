package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum SimpleAuditStatus implements ValueObject<SimpleAuditStatus>, EnumMessage<String> {
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
