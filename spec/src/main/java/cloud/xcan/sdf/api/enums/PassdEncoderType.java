package cloud.xcan.sdf.api.enums;

import cloud.xcan.sdf.spec.ValueObject;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

@EndpointRegister
public enum PassdEncoderType implements ValueObject<PassdEncoderType>, EnumValueMessage<String> {
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

  PassdEncoderType(String value) {
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
