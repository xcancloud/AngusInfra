package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * User data isolation scope under tenant.
 */
@EndpointRegister
public enum DataScope implements EnumMessage<String> {
  PUBLIC, CREATOR, DEPT, GROUP;

  @Override
  public String getValue() {
    return this.name();
  }

}
