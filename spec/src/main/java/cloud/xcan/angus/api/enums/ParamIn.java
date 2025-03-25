package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

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
