package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum Percentile implements Value<String> {
  P50, P75, P90, P95, P99, P999, ALL;

  @Override
  public String getValue() {
    return this.name();
  }

}
