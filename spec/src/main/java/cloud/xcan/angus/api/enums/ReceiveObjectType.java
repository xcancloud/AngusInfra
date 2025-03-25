package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ReceiveObjectType implements EnumMessage<String> {
  TENANT, DEPT, GROUP, USER, POLICY, TO_POLICY, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
