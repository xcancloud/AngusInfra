package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

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
