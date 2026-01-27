package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum ProductConsumerType implements Value<String> {

  USER, SYSTEM;

  @Override
  public String getValue() {
    return this.name();
  }

}
