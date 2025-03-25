package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ProductConsumerType implements EnumMessage<String> {

  USER, SYSTEM;

  @Override
  public String getValue() {
    return this.name();
  }

}
