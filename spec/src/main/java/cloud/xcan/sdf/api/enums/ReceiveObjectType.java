package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ReceiveObjectType implements EnumMessage<String> {
  TENANT, DEPT, GROUP, USER, POLICY, TO_POLICY, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
