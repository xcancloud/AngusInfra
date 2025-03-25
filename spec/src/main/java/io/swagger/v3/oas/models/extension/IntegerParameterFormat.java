package io.swagger.v3.oas.models.extension;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@ThirdExtension
@EndpointRegister
public enum IntegerParameterFormat implements EnumValueMessage<String> {

  //-, // Any numbers.

  /**
   * Signed 32-bit integers (commonly used integer type).
   */
  int32,

  /**
   * Signed 64-bit integers (long type).
   */
  int64;

  @Override
  public String getValue() {
    return this.name();
  }
}
