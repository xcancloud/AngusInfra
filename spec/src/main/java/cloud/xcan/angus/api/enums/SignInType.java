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

}
