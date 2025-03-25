package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@EndpointRegister
public enum AuthIn implements ValueObject<AuthIn>, EnumValueMessage<String> {
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
