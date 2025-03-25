package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum FileResourceType implements EnumMessage<String> {
  SPACE, DIRECTORY, FILE;

  @Override
  public String getValue() {
    return this.name();
  }
}
