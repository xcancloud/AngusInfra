package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;


@EndpointRegister
public enum SignInType implements EnumMessage<String> {

  ACCOUNT_PASSWORD,
  SMS_CODE,
  EMAIL_CODE,
  THIRD_SOCIAL;

  @Override
  public String getValue() {
    return this.name();
  }

  public String toOAuth2GrantType() {
    return switch (this) {
      case ACCOUNT_PASSWORD -> "PASSWORD";
      case SMS_CODE -> "SMS_CODE";
      case EMAIL_CODE -> "EMAIL_CODE";
      case THIRD_SOCIAL -> "THIRD_SOCIAL";
    };
  }

}
