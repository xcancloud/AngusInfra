package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AuditStatus implements EnumMessage<String> {
  PENDING,
  APPROVED,
  NOT_APPROVED;

  @Override
  public String getValue() {
    return this.name();
  }
}
