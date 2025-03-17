package io.swagger.v3.oas.models.extension;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

/**
 * Apis protocol.
 *
 * @author XiaoLong Liu
 */
@ThirdExtension
@EndpointRegister
public enum ApisProtocol implements EnumValueMessage<String> {
  http, https, ws, wss;

  @Override
  public String getValue() {
    return this.name().toLowerCase();
  }

  @Override
  public String getMessage() {
    return getValue();
  }

  public boolean isHttp() {
    return this.equals(http) || this.equals(https);
  }

  public boolean isWebSocket() {
    return this.equals(ws) || this.equals(wss);
  }
}
