package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@EndpointRegister
public enum SocialType implements ValueObject<SocialType>, EnumValueMessage<String> {

  WECHAT,
  GITHUB,
  GOOGLE;

  @Override
  public String getValue() {
    return this.name();
  }
}
