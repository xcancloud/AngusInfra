package cloud.xcan.sdf.api.enums;


import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.util.Objects;

/**
 * Authorization types supported by XCan.
 */
@EndpointRegister
public enum GrantType implements EnumMessage<String> {

  IMPLICIT, REFRESH_TOKEN, CLIENT_CREDENTIALS, PASSWORD, THIRD_SOCIAL;

  @Override
  public String getValue() {
    return this.name();
  }

  public String getLowerValue() {
    return this.name().toLowerCase();
  }

  public String getCameValue(){
    return StringUtils.underToCamel(this.getValue());
  }

  public static GrantType of(String value) {
    if (Objects.isNull(value)) {
      return null;
    }
    GrantType[] grantTypes = values();
    for (GrantType grantType : grantTypes) {
      if (grantType.getValue().equalsIgnoreCase(value)) {
        return grantType;
      }
    }
    return null;
  }


}
