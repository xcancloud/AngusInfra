package cloud.xcan.angus.spec.unit;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.utils.StringUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import lombok.Getter;

/**
 * A numeric rate; {@link #parse(String)} accepts a trailing {@code %} (the value is stored as the
 * parsed number; formatting can re-append {@code %} when parsed that way).
 */
@Getter
public final class RateValue {

  private static final DecimalFormat HUMAN = new DecimalFormat("0.00",
      DecimalFormatSymbols.getInstance(Locale.ROOT));

  private final double value;
  /** {@code true} if the value was parsed from a string ending with {@code %}. */
  private final boolean percentSuffix;

  public RateValue() {
    this(0d, false);
  }

  public RateValue(double value) {
    this(value, false);
  }

  private RateValue(double value, boolean percentSuffix) {
    this.value = value;
    this.percentSuffix = percentSuffix;
  }

  public static RateValue parse(String value) {
    if (isEmpty(value)) {
      return new RateValue();
    }

    String value0 = StringUtils.removeSpace(value);
    if (value0.endsWith("%")) {
      String num = value0.substring(0, value0.length() - 1);
      if (num.isEmpty()) {
        throw new IllegalArgumentException("Invalid rate: '" + value + "'");
      }
      return new RateValue(Double.parseDouble(num), true);
    }
    return new RateValue(Double.parseDouble(value0), false);
  }

  public String toHumanString() {
    String s = HUMAN.format(value);
    return percentSuffix ? s + "%" : s;
  }

  @Override
  public String toString() {
    return percentSuffix ? value + "%" : String.valueOf(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RateValue that)) {
      return false;
    }
    return Double.doubleToLongBits(value) == Double.doubleToLongBits(that.value)
        && percentSuffix == that.percentSuffix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, percentSuffix);
  }
}
