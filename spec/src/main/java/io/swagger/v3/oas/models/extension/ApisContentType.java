package io.swagger.v3.oas.models.extension;

import static java.util.Objects.nonNull;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

/**
 * @author XiaoLong Liu
 */
@ThirdExtension
@EndpointRegister
public enum ApisContentType implements EnumValueMessage<String> {
  /**
   * No body and ContentType in header.
   */
  JSON("application/json", true),
  HTML("text/html", true),
  XML("application/xml", true),
  JAVASCRIPT("application/javascript", true),
  TEXT("text/plain", true),
  FORM_URLENCODED("application/x-www-form-urlencoded", true),
  FORM_DATA("multipart/form-data", true),
  BINARY("application/octet-stream", false);

  private final String code;
  private final boolean extractable;

  ApisContentType(String code, boolean extractable) {
    this.code = code;
    this.extractable = extractable;
  }

  public static boolean isSupported(String contentType) {
    if (nonNull(contentType)){
      for (ApisContentType type : values()) {
        if (type.extractable && contentType.startsWith(type.code)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isRawContent(String contentType) {
    return JSON.code.equals(contentType) || XML.code.equals(contentType)
        || HTML.code.equals(contentType) || JAVASCRIPT.code.equals(contentType)
        || TEXT.code.equals(contentType);
  }

  public static boolean isForm(String contentType) {
    return isFormUrlEncoded(contentType) || isFormData(contentType);
  }

  public static boolean isFormUrlEncoded(String contentType) {
    return FORM_URLENCODED.code.equals(contentType);
  }

  public static boolean isFormData(String contentType) {
    return FORM_DATA.code.equals(contentType);
  }

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.code;
  }

  public String getCode() {
    return this.code;
  }
}
