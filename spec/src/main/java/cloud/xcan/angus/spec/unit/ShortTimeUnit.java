package cloud.xcan.angus.spec.unit;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

@EndpointRegister
public enum ShortTimeUnit implements EnumValueMessage<String> {
  Millisecond("ms"),
  Second("s"),
  Minute("min"),
  Hour("h"),
  Day("d");

  private final String suffix;

  ShortTimeUnit(String suffix) {
    Assert.assertNotEmpty(suffix, "Suffix cannot be empty");
    this.suffix = suffix;
  }

  /**
   * Return the {@link ShortTimeUnit} matching the specified {@code suffix}.
   *
   * @param suffix one of the standard suffixes
   * @return the {@link ShortTimeUnit} matching the specified {@code suffix}
   * @throws IllegalArgumentException if the suffix does not match the suffix of any of this enum's
   *                                  constants
   */
  public static ShortTimeUnit fromSuffix(String suffix) {
    for (ShortTimeUnit candidate : values()) {
      if (candidate.suffix.equalsIgnoreCase(suffix)) {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Unknown data unit suffix '" + suffix + "'");
  }

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return this.suffix;
  }
}
