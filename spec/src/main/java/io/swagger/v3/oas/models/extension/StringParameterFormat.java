package io.swagger.v3.oas.models.extension;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

@ThirdExtension
@EndpointRegister
public enum StringParameterFormat implements EnumValueMessage<String> {
  ////////////// OpenAPI defines the following built-in string formats //////////////
  /**
   * OpenAPI 3.0 does not have an explicit null type as in JSON Schema, but you can use nullable:
   * true to specify that the value may be null. Note that null is different from an empty string
   * "".
   * <pre>
   * # Correct
   * type: integer
   * nullable: true
   *
   * # Incorrect
   * type: null
   *
   * # Incorrect as well
   * type:
   *   - integer
   *   - null
   * </pre>
   */
  @JsonProperty("null")
  _null,
  /**
   * Binary data, used to describe files (see Files below):
   * <pre>
   * Unlike OpenAPI 2.0, Open API 3.0 does not have the file type. Files are defined as strings:
   * type: string
   * format: binary  # binary file contents
   * or
   * type: string
   * format: byte    # base64-encoded file contents
   * </pre>
   */
  binary,
  /**
   * Base64-encoded characters, for example, U3dhZ2dlciByb2Nrcw==
   */
  _byte,
  /**
   * Full-date notation as defined by RFC 3339, section 5.6, for example, 2017-07-21
   */
  date,
  /**
   * The date-time notation as defined by RFC 3339, section 5.6, for example, 2017-07-21T17:32:28Z
   */
  @JsonProperty("date-time")
  date_time,
  /**
   * A hint to UIs to mask the input
   */
  password,

  ////////////// However, format is an open value, so you can use any formats, even not those defined by the OpenAPI Specification, such as: //////////////
  email,
  uuid,
  uri,
  hostname,
  ipv4,
  ipv6;

  @Override
  public String getValue() {
    if (this.equals(_null)) {
      return "null";
    }
    if (this.equals(date_time)) {
      return "date-time";
    }
    return this.name();
  }
}
