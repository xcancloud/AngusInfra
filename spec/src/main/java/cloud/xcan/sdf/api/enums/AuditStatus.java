package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
