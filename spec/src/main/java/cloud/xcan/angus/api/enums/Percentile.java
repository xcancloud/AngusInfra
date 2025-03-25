package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum Percentile implements EnumMessage<String> {
  P50, P75, P90, P95, P99, P999, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
