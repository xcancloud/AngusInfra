package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;


public enum NodeRole implements Value<String> {
  MANAGEMENT,
  CONTROLLER,
  EXECUTION,
  MOCK_SERVICE,
  APPLICATION;

  @Override
  public String getValue() {
    return this.name();
  }

}
