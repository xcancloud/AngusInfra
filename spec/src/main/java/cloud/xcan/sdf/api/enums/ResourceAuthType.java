package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * Resource access scope.
 */
@EndpointRegister
public enum ResourceAuthType implements EnumMessage<String> {
  API, ACL;

  @Override
  public String getValue() {
    return this.name();
  }
}
