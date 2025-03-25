package cloud.xcan.angus.spec.unit;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * A standard set of {@link DataSize} units.
 *
 * <p>The unit prefixes used in this class are
 * <a href="https://en.wikipedia.org/wiki/Binary_prefix">binary prefixes</a>
 * indicating multiplication by powers of 2. The following table displays the enum constants defined
 * in this class and corresponding values.
 *
 * <p>
 * <table border="1">
 * <tr><th>Constant</th><th>Data Size</th><th>Power&nbsp;of&nbsp;2</th><th>Size in Bytes</th></tr>
 * <tr><td>{@link #Bytes}</td><td>1B</td><td>2^0</td><td>1</td></tr>
 * <tr><td>{@link #Kilobytes}</td><td>1KB</td><td>2^10</td><td>1,024</td></tr>
 * <tr><td>{@link #Megabytes}</td><td>1MB</td><td>2^20</td><td>1,048,576</td></tr>
 * <tr><td>{@link #Gigabytes}</td><td>1GB</td><td>2^30</td><td>1,073,741,824</td></tr>
 * <tr><td>{@link #Terabytes}</td><td>1TB</td><td>2^40</td><td>1,099,511,627,776</td></tr>
 * </table>
 *
 * @see DataSize
 */
@EndpointRegister
public enum DataUnit implements EnumValueMessage<String> {

  /**
   * Bytes, represented by suffix {@code B}.
   */
  Bytes("B"),

  /**
   * Kilobytes, represented by suffix {@code KB}.
   */
  Kilobytes("KB"),

  /**
   * Megabytes, represented by suffix {@code MB}.
   */
  Megabytes("MB"),

  /**
   * Gigabytes, represented by suffix {@code GB}.
   */
  Gigabytes("GB"),

  /**
   * Terabytes, represented by suffix {@code TB}.
   */
  Terabytes("TB");

  private final String suffix;

  DataUnit(String suffix) {
    Assert.assertNotEmpty(suffix, "Suffix cannot be empty");
    this.suffix = suffix;
  }

  /**
   * Return the {@link DataUnit} matching the specified {@code suffix}.
   *
   * @param suffix one of the standard suffixes
   * @return the {@link DataUnit} matching the specified {@code suffix}
   * @throws IllegalArgumentException if the suffix does not match the suffix of any of this enum's
   *                                  constants
   */
  public static DataUnit fromSuffix(String suffix) {
    for (DataUnit candidate : values()) {
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
