package io.swagger.v3.oas.models.extension;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

@ThirdExtension
@EndpointRegister
public enum NumberParameterFormat implements EnumValueMessage<String> {

  //-, // Any numbers.

  /**
   * Floating-point numbers.
   */
  @JsonProperty("float")
  _float,

  /**
   * Floating-point numbers with double precision.
   */
  @JsonProperty("double")
  _double;

  @Override
  public String getValue() {
    if (this.equals(_float)) {
      return "float";
    }
    if (this.equals(_double)) {
      return "double";
    }
    return this.name();
  }
}
