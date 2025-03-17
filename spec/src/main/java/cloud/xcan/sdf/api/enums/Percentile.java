package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum Percentile implements EnumMessage<String> {
  P50, P75, P90, P95, P99, P999, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
