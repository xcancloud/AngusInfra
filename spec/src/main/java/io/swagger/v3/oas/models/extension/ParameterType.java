package io.swagger.v3.oas.models.extension;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameter format of type.
 * <pre>
 * - string (this includes dates and files) - StringParameterFormat(enum)
 *   - date
 *   - date-time
 *   - password
 *   - byte
 *   - binary
 *   - email
 *   - uuid
 *   - uri
 *   - hostname
 *   - ipv4
 *   - ipv6
 *   - other...
 * - number - NumberParameterFormat(enum)
 *   - -
 *   - float
 *   - double
 * - integer - IntegerParameterFormat(enum)
 *   - -
 *   - int32
 *   - int64
 * - boolean
 * - array
 * - object
 * </pre>
 */
@ThirdExtension
@EndpointRegister
public enum ParameterType implements EnumValueMessage<String> {
  array,
  /**
   * this includes dates and files.
   */
  string,
  @JsonProperty("boolean")
  boolean_,
  integer,
  object,
  number;

  @Override
  public String getValue() {
    return this.equals(boolean_) ? "boolean" : this.name();
  }

  public static boolean isObject(String type) {
    return object.getValue().equalsIgnoreCase(type);
  }

  public static boolean isArray(String type) {
    return array.getValue().equalsIgnoreCase(type);
  }

  public static boolean isPrimitive(String type) {
    return !isObject(type) && !isArray(type);
  }
}
