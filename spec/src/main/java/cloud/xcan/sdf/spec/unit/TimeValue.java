package cloud.xcan.sdf.spec.unit;


import static cloud.xcan.sdf.spec.experimental.Assert.assertState;

import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see `org.openjdk.jmh.runner.options.TimeValue`
 */
public class TimeValue implements ValueUnit<Long, ShortTimeUnit> {

  /**
   * The pattern for parsing.
   */
  private static final Pattern PATTERN = Pattern.compile("^(\\d+)([a-zA-Z]{0,3})$");

  /**
   * Millisecond per second.
   */
  private static final long MILLISECOND_PER_SECOND = 1000;

  /**
   * Millisecond per Minute.
   */
  private static final long MILLISECOND_PER_MINUTE = MILLISECOND_PER_SECOND * 60;

  /**
   * Millisecond per Hour.
   */
  private static final long MILLISECOND_PER_HOUR = MILLISECOND_PER_MINUTE * 60;

  /**
   * Millisecond per Day.
   */
  private static final long MILLISECOND_PER_DAY = MILLISECOND_PER_HOUR * 24;

  private long value;

  private ShortTimeUnit unit;

  public TimeValue() {
    this.value = 0L;
    this.unit = ShortTimeUnit.Millisecond;
  }

  public TimeValue(long value) {
    this.value = value;
    this.unit = ShortTimeUnit.Millisecond;
  }

  public TimeValue(long value, ShortTimeUnit unit) {
    Assert.assertNotNull(unit, "unit cannot be empty");
    this.value = value;
    this.unit = unit;
  }

  public static TimeValue valueOf(String value) {
    return parse(value);
  }

  public static TimeValue ofDays(long v) {
    return new TimeValue(v, ShortTimeUnit.Day);
  }

  public static TimeValue ofHours(long v) {
    return new TimeValue(v, ShortTimeUnit.Hour);
  }

  public static TimeValue ofMinutes(long v) {
    return new TimeValue(v, ShortTimeUnit.Minute);
  }

  public static TimeValue ofSeconds(long v) {
    return new TimeValue(v, ShortTimeUnit.Second);
  }

  public static TimeValue ofMillisecond(long v) {
    return new TimeValue(v, ShortTimeUnit.Millisecond);
  }

  public static TimeValue of(long v, ShortTimeUnit unit) {
    Assert.assertNotNull(unit, "unit cannot be empty");
    return new TimeValue(v, unit);
  }

  public double toDays() {
    return (double) toMilliSecond() / MILLISECOND_PER_DAY;
  }

  public double toHours() {
    return (double) toMilliSecond() / MILLISECOND_PER_HOUR;
  }

  public double toMinutes() {
    return (double) toMilliSecond() / MILLISECOND_PER_MINUTE;
  }

  public double toSecond() {
    return (double) toMilliSecond() / MILLISECOND_PER_SECOND;
  }

  public long toMilliSecond() {
    switch (this.unit) {
      case Day:
        return value * MILLISECOND_PER_DAY;
      case Hour:
        return value * MILLISECOND_PER_HOUR;
      case Minute:
        return value * MILLISECOND_PER_MINUTE;
      case Second:
        return value * MILLISECOND_PER_SECOND;
      default: // MS
        return value;
    }
  }

  public String formatDays() {
    return toDays() + ShortTimeUnit.Day.getMessage();
  }

  public String formatHours() {
    return toHours() + ShortTimeUnit.Hour.getMessage();
  }

  public String formatMinutes() {
    return toMinutes() + ShortTimeUnit.Minute.getMessage();
  }

  public String formatSecond() {
    return toSecond() + ShortTimeUnit.Second.getMessage();
  }

  public String formatMilliSecond() {
    return toMilliSecond() + ShortTimeUnit.Millisecond.getValue();
  }

  @Override
  public String toString() {
    return value + unit.getMessage();
  }

  @Override
  public String toHumanString() {
    double value = toSecond();
    if (value < 1) {
      return getFormatString(toMilliSecond(), ShortTimeUnit.Millisecond);
    }
    value = toMinutes();
    if (value < 1) {
      return getFormatString(toSecond(), ShortTimeUnit.Second);
    }
    value = toHours();
    if (value < 1) {
      return getFormatString(toMinutes(), ShortTimeUnit.Minute);
    }
    value = toDays();
    if (value < 1) {
      return getFormatString(toHours(), ShortTimeUnit.Hour);
    }
    return getFormatString(value, ShortTimeUnit.Day);
  }

  public String toHumanString(ShortTimeUnit unit) {
    switch (unit) {
      case Millisecond:
        return toMilliSecond() + unit.getMessage();
      case Second:
        return toSecond() + unit.getMessage();
      case Minute:
        return toMinutes() + unit.getMessage();
      case Hour:
        return toHours() + unit.getMessage();
      case Day:
        return toDays() + unit.getMessage();
    }
    return getFormatString(value, ShortTimeUnit.Day);
  }

  private String getFormatString(double size, ShortTimeUnit unit) {
    BigDecimal value = new BigDecimal(size);
    double result = value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    return result + unit.getMessage();
  }

  public static TimeValue parse(CharSequence text) {
    return parse(text, null);
  }

  public static TimeValue parse(CharSequence text, ShortTimeUnit defaultUnit) {
    Assert.assertNotNull(text, "Text must not be null");
    try {
      Matcher matcher = PATTERN.matcher(text);
      assertState(matcher.matches(), "Does not match time value pattern");
      ShortTimeUnit unit = determineTimeUnit(matcher.group(2), defaultUnit);
      long amount = Long.parseLong(matcher.group(1));
      return TimeValue.of(amount, unit);
    } catch (Exception ex) {
      throw new IllegalArgumentException("'" + text + "' is not a valid time value", ex);
    }
  }

  private static ShortTimeUnit determineTimeUnit(String suffix, ShortTimeUnit defaultUnit) {
    ShortTimeUnit defaultUnitToUse = (defaultUnit != null ? defaultUnit
        : ShortTimeUnit.Millisecond);
    return (StringUtils.hasLength(suffix) ? ShortTimeUnit.fromSuffix(suffix) : defaultUnitToUse);
  }

  @Override
  public Long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  @Override
  public ShortTimeUnit getUnit() {
    return unit;
  }

  public void setUnit(ShortTimeUnit unit) {
    this.unit = unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TimeValue)) {
      return false;
    }
    TimeValue timeValue = (TimeValue) o;
    return (value == timeValue.value && unit == timeValue.unit)
        || timeValue.toMilliSecond() == toMilliSecond();
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, unit);
  }
}