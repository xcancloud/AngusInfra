package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

@EndpointRegister
public enum ParamIn implements ValueObject<ParamIn>, EnumValueMessage<String> {
  cookie,
  header,
  query,
  path;

  @Override
  public String getValue() {
    return this.name();
  }
}
