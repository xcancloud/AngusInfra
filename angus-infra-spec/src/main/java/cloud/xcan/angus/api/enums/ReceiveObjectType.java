package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ReceiveObjectType implements Value<String> {
  TENANT, DEPT, GROUP, USER, POLICY, TO_POLICY, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
