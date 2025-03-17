package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

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
