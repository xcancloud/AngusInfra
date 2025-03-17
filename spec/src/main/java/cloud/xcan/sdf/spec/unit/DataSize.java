package cloud.xcan.sdf.spec.unit;

import static cloud.xcan.sdf.spec.experimental.Assert.assertState;

import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A data size, such as '12MB'.
 *
 * <p>This class models data size in terms of bytes and is immutable and thread-safe.
 *
 * <p>The terms and units used in this class are based on
 * <a href="https://en.wikipedia.org/wiki/Binary_prefix">binary prefixes</a>
 * indicating multiplication by powers of 2. Consult the following table and the Javadoc for {@link
 * DataUnit} for details.
 *
 * <p>
 * <table border="1">
 * <tr><th>Term</th><th>Data Size</th><th>Size in Bytes</th></tr>
 * <tr><td>byte</td><td>1B</td><td>1</td></tr>
 * <tr><td>kilobyte</td><td>1KB</td><td>1,024</td></tr>
 * <tr><td>megabyte</td><td>1MB</td><td>1,048,576</td></tr>
 * <tr><td>gigabyte</td><td>1GB</td><td>1,073,741,824</td></tr>
 * <tr><td>terabyte</td><td>1TB</td><td>1,099,511,627,776</td></tr>
 * </table>
 *
 * @see DataUnit
 */
@SuppressWarnings("serial")
public final class DataSize implements Comparable<DataSize>, ValueUnit<Double, DataUnit> {

  /**
   * The pattern for parsing.
   */
  private static final Pattern PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(\\w+)");

  /**
   * Bytes per Kilobyte.
   */
  private static final long BYTES_PER_KB = 1024;

  /**
   * Bytes per Megabyte.
   */
  private static final long BYTES_PER_MB = BYTES_PER_KB * 1024;

  /**
   * Bytes per Gigabyte.
   */
  private static final long BYTES_PER_GB = BYTES_PER_MB * 1024;

  /**
   * Bytes per Terabyte.
   */
  private static final long BYTES_PER_TB = BYTES_PER_GB * 1024;

  private final double value;
  private final DataUnit unit;

  public DataSize() {
    this.value = 0d;
    this.unit = DataUnit.Bytes;
  }

  public DataSize(double value, DataUnit unit) {
    Assert.assertNotNull(unit, "unit cannot be empty");
    this.value = value;
    this.unit = unit;
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of bytes.
   *
   * @param bytes the number of bytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofBytes(long bytes) {
    return new DataSize(bytes, DataUnit.Bytes);
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of bytes.
   *
   * @param bytes the number of bytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofBytes(double bytes) {
    return new DataSize(bytes, DataUnit.Bytes);
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of kilobytes.
   *
   * @param kilobytes the number of kilobytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofKilobytes(double kilobytes) {
    return new DataSize(kilobytes, DataUnit.Kilobytes);
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of megabytes.
   *
   * @param megabytes the number of megabytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofMegabytes(double megabytes) {
    return new DataSize(megabytes, DataUnit.Megabytes);
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of gigabytes.
   *
   * @param gigabytes the number of gigabytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofGigabytes(double gigabytes) {
    return new DataSize(gigabytes, DataUnit.Gigabytes);
  }

  /**
   * Obtain a {@link DataSize} representing the specified number of terabytes.
   *
   * @param terabytes the number of terabytes, positive or negative
   * @return a {@link DataSize}
   */
  public static DataSize ofTerabytes(double terabytes) {
    return new DataSize(terabytes, DataUnit.Terabytes);
  }

  /**
   * Obtain a {@link DataSize} representing an amount in the specified {@link DataUnit}.
   *
   * @param amount the amount of the size, measured in terms of the unit, positive or negative
   * @return a corresponding {@link DataSize}
   */
  public static DataSize of(double amount, DataUnit unit) {
    Assert.assertNotNull(unit, "unit cannot be empty");
    return new DataSize(amount, unit);
  }


  /**
   * Checks if this size is negative, excluding zero.
   *
   * @return true if this size has a size less than zero bytes
   */
  public boolean negative() {
    return this.toBytes() < 0;
  }

  /**
   * Return the number of bytes in this instance.
   *
   * @return the number of bytes
   */
  public long toBytes() {
    switch (unit) {
      case Terabytes:
        return (long) (value * BYTES_PER_TB);
      case Gigabytes:
        return (long) (value * BYTES_PER_GB);
      case Megabytes:
        return (long) (value * BYTES_PER_MB);
      case Kilobytes:
        return (long) (value * BYTES_PER_KB);
      default:
        return (long) value;
    }
  }

  /**
   * Return the number of kilobytes in this instance.
   *
   * @return the number of kilobytes
   */
  public double toKilobytes() {
    return (double) this.toBytes() / BYTES_PER_KB;
  }

  /**
   * Return the number of megabytes in this instance.
   *
   * @return the number of megabytes
   */
  public double toMegabytes() {
    return (double) this.toBytes() / BYTES_PER_MB;
  }

  /**
   * Return the number of gigabytes in this instance.
   *
   * @return the number of gigabytes
   */
  public double toGigabytes() {
    return (double) this.toBytes() / BYTES_PER_GB;
  }

  /**
   * Return the number of terabytes in this instance.
   *
   * @return the number of terabytes
   */
  public double toTerabytes() {
    return (double) this.toBytes() / BYTES_PER_TB;
  }

  /**
   * Obtain a {@link DataSize} from a text string such as {@code 12MB} using {@link DataUnit#Bytes}
   * if no unit is specified.
   * <p>
   * Examples:
   * <pre>
   * "12KB" -- parses as "12 kilobytes"
   * "5MB"  -- parses as "5 megabytes"
   * "20"   -- parses as "20 bytes"
   * </pre>
   *
   * @param text the text to parse
   * @return the parsed {@link DataSize}
   * @see #parse(CharSequence, DataUnit)
   */
  public static DataSize parse(CharSequence text) {
    return parse(text, null);
  }

  /**
   * Obtain a {@link DataSize} from a text string such as {@code 12MB} using the specified default
   * {@link DataUnit} if no unit is specified.
   * <p>
   * The string starts with a number followed optionally by a unit matching one of the supported
   * {@linkplain DataUnit suffixes}.
   * <p>
   * Examples:
   * <pre>
   * "12KB" -- parses as "12 kilobytes"
   * "5MB"  -- parses as "5 megabytes"
   * "20"   -- parses as "20 kilobytes" (where the {@code defaultUnit} is {@link DataUnit#Kilobytes})
   * </pre>
   *
   * @param text the text to parse
   * @return the parsed {@link DataSize}
   */
  public static DataSize parse(CharSequence text, DataUnit defaultUnit) {
    Assert.assertNotNull(text, "Text must not be null");
    try {
      Matcher matcher = PATTERN.matcher(text);
      assertState(matcher.matches(), "Does not match data size pattern");
      DataUnit unit = determineDataUnit(matcher.group(3), defaultUnit);
      double amount = Double.parseDouble(matcher.group(1));
      return DataSize.of(amount, unit);
    } catch (Exception ex) {
      throw new IllegalArgumentException("'" + text + "' is not a valid data size", ex);
    }
  }

  private static DataUnit determineDataUnit(String suffix, DataUnit defaultUnit) {
    DataUnit defaultUnitToUse = (defaultUnit != null ? defaultUnit : DataUnit.Bytes);
    return (StringUtils.hasLength(suffix) ? DataUnit.fromSuffix(suffix) : defaultUnitToUse);
  }

  @Override
  public String toHumanString() {
    double size = toKilobytes();
    if (size < 1) {
      return getFormatString(toBytes(), DataUnit.Bytes);
    }
    size = toMegabytes();
    if (size < 1) {
      return getFormatString(toKilobytes(), DataUnit.Kilobytes);
    }
    size = toGigabytes();
    if (size < 1) {
      return getFormatString(toMegabytes(), DataUnit.Megabytes);
    }
    size = toTerabytes();
    if (size < 1) {
      return getFormatString(toGigabytes(), DataUnit.Gigabytes);
    }
    return getFormatString(size, DataUnit.Terabytes);
  }

  private String getFormatString(double size, DataUnit unit) {
    BigDecimal value = new BigDecimal(size);
    double result = value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    return result + unit.getMessage();
  }

  @Override
  public Double getValue() {
    return value;
  }

  @Override
  public DataUnit getUnit() {
    return unit;
  }

  @Override
  public int compareTo(DataSize other) {
    return Long.compare(this.toBytes(), other.toBytes());
  }

  @Override
  public String toString() {
    return value + unit.getMessage();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    DataSize otherSize = (DataSize) other;
    return (this.toBytes() == otherSize.toBytes());
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.toBytes());
  }

}
