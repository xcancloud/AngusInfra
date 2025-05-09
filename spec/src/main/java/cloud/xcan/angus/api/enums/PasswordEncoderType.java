package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@EndpointRegister
public enum PasswordEncoderType implements ValueObject<PasswordEncoderType>, EnumValueMessage<String> {
  PLAINTEXT("noop"),
  BCRYPT("bcrypt"),
  MD4("MD4"),
  MD5("MD5"),
  PBKDF2("pbkdf2"),
  SCRYPT("scrypt"),
  SHA1("SHA-1"),
  SHA256("SHA-256"),
  ARGON2("argon2");

  String value;

  PasswordEncoderType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public String getMessage() {
    return this.name();
  }
}
