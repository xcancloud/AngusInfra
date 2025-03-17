package cloud.xcan.sdf.spec.experimental;

import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.obf.Str0;
import cloud.xcan.sdf.api.pojo.Pair;
import cloud.xcan.sdf.spec.utils.crypto.AESUtils;

public class AESValue implements Value<String> {

  private final String algorithm = new Str0(new long[]{0xC20CFE9D60E34702L, 0x8D5D0C04EAC2FFC4L})
      .toString() /* => "AES" */;

  private String value;

  private final String ALGORITHM_MATCHER = "{" + algorithm + "}";

  /**
   * AES password encryption key
   */
  private final static String VALUE_SAT = new Str0(
      new long[]{0xC5C3F6C7C8FE66B2L, 0x59A3353D5B643935L})
      .toString() /* => "XCAN" */ /*+ tenantId*/;

  public AESValue() {
  }

  public AESValue(String value) {
    this.value = value;
  }

  public AESValue setValue(String value) {
    this.value = value;
    return this;
  }

  public AESValue setValue(String value, String password) {
    this.value = value;
    return encrypt(password);
  }

  @Override
  public String getValue() {
    return value;
  }

  public AESValue encrypt(String password) {
    if (nonNull(this.value) && !value.startsWith(ALGORITHM_MATCHER)) {
      this.value = ALGORITHM_MATCHER + AESUtils.encrypt(Pair.of(VALUE_SAT + password, value));
    }
    return this;
  }

  public String decrypt(String password) {
    if (nonNull(value) && value.startsWith(ALGORITHM_MATCHER)) {
      return AESUtils.decrypt(Pair.of(VALUE_SAT + password,
          value.substring(ALGORITHM_MATCHER.length())));
    }
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
