package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * Resource ACL type.
 */
@EndpointRegister
public enum ResourceAclType implements EnumMessage<String> {
  ALL, ONLY_READ, ONLY_WRITE, READ_WRITE, DELETE;

  @Override
  public String getValue() {
    return this.name();
  }
}
