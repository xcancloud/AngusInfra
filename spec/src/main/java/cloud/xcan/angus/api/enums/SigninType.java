package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * XCan account login type.
 */
@EndpointRegister
public enum SigninType implements EnumMessage<String> {

  ACCOUNT_PASSD,
  MOBILE_SMS,
  THIRD_SOCIAL
 /*, SOCIAL_GITHUB,
  SOCIAL_GOOGLE,
  SOCIAL_WECHAT
  SOCIAL_WECHAT_MINIAPP("wechat_miniapp")*/;

  @Override
  public String getValue() {
    return this.name();
  }

}
