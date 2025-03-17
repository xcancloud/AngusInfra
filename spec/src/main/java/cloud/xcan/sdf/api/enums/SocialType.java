package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

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
