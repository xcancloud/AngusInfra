package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * XCan default user role.
 */
@EndpointRegister
public enum DefaultRoleType implements EnumMessage<String> {

  SYS_ADMIN, APP_ADMIN, NORMAL, GUEST, ANONYMOUS;

  @Override
  public String getValue() {
    return this.name();
  }

}
