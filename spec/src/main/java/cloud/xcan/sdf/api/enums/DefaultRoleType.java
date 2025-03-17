package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
