package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;

public enum AuthIn implements Value<String> {
  //cookie,
  header,
  query;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isHeader() {
    return this.equals(header);
  }

  public boolean isQuery() {
    return this.equals(query);
  }
}
