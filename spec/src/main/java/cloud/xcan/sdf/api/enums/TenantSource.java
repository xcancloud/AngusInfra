package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
