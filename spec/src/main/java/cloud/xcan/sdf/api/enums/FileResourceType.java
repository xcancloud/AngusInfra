package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum FileResourceType implements EnumMessage<String> {
  SPACE, DIRECTORY, FILE;

  @Override
  public String getValue() {
    return this.name();
  }
}
