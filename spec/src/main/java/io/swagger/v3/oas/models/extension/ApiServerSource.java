package io.swagger.v3.oas.models.extension;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

@ThirdExtension
@EndpointRegister
public enum ApiServerSource implements EnumValueMessage<String> {
  CURRENT_REQUEST, API_SERVERS, PARENT_SERVERS, MOCK_SERVICE;

  @Override
  public String getValue() {
    return this.name();
  }
}
