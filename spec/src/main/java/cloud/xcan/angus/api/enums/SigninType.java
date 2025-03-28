package cloud.xcan.angus.api.enums;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;


@EndpointRegister
public enum SigninType implements EnumMessage<String> {

  ACCOUNT_PASSWORD,
  MOBILE_SMS,
  THIRD_SOCIAL;

  @Override
  public String getValue() {
    return this.name();
  }

}
