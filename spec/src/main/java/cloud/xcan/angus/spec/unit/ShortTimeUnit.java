package cloud.xcan.angus.spec.unit;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.Value;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;

@Getter
public enum ShortTimeUnit implements Value<String> {
  Millisecond("ms"),
  Second("s"),
  Minute("min"),
  Hour("h"),
  Day("d");

  private static final Map<String, ShortTimeUnit> BY_SUFFIX_LC = initSuffixMap();

  private final String suffix;

  ShortTimeUnit(String suffix) {
    Assert.assertNotEmpty(suffix, "Suffix cannot be empty");
    this.suffix = suffix;
  }

  private static Map<String, ShortTimeUnit> initSuffixMap() {
    Map<String, ShortTimeUnit> m = new HashMap<>();
    for (ShortTimeUnit u : values()) {
      m.put(u.suffix.toLowerCase(Locale.ROOT), u);
    }
    return Map.copyOf(m);
  }

  /**
   * Return the {@link ShortTimeUnit} matching the specified {@code suffix} (case-insensitive).
   *
   * @param suffix one of the standard suffixes
   * @return the {@link ShortTimeUnit} matching the specified {@code suffix}
   * @throws IllegalArgumentException if the suffix does not match the suffix of any of this enum's
   *                                  constants
   */
  public static ShortTimeUnit fromSuffix(String suffix) {
    Assert.assertNotNull(suffix, "suffix");
    ShortTimeUnit unit = BY_SUFFIX_LC.get(suffix.toLowerCase(Locale.ROOT));
    if (unit == null) {
      throw new IllegalArgumentException("Unknown time unit suffix '" + suffix + "'");
    }
    return unit;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
