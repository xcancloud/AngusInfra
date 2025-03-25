package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

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
