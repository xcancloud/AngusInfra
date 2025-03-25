package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum TenantSource implements EnumMessage<String> {
  PLATFORM_SIGNUP,
  BACKGROUND_SIGNUP,
  THIRD_PARTY_LOGIN;

  @Override
  public String getValue() {
    return this.name();
  }
}
