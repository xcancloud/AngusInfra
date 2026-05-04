package cloud.xcan.angus.spec.unit;


import static cloud.xcan.angus.spec.experimental.Assert.assertState;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable duration as a (value, {@link ShortTimeUnit}) pair; conversions normalize to
 * milliseconds.
 *
 * @see org.openjdk.jmh.runner.options.TimeValue (conceptual analogue)
 */
public final class TimeValue implements ValueUnit<Long, ShortTimeUnit>, Comparable<TimeValue> {

  private static final Pattern PATTERN = Pattern.compile("^\\s*(\\d+)([a-zA-Z]{0,3})\\s*$");

  private static final long MILLISECOND_PER_SECOND = 1000;

  private static final long MILLISECOND_PER_MINUTE = MILLISECOND_PER_SECOND * 60;

  private static final long MILLISECOND_PER_HOUR = MILLISECOND_PER_MINUTE * 60;

  private static final long MILLISECOND_PER_DAY = MILLISECOND_PER_HOUR * 24;

  private final long value;

  private final ShortTimeUnit unit;

  public TimeValue() {
    this(0L, ShortTimeUnit.Millisecond);
  }

  public TimeValue(long value) {
    this(value, ShortTimeUnit.Millisecond);
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
    return switch (this.unit) {
      case Day -> value * MILLISECOND_PER_DAY;
      case Hour -> value * MILLISECOND_PER_HOUR;
      case Minute -> value * MILLISECOND_PER_MINUTE;
      case Second -> value * MILLISECOND_PER_SECOND;
      case Millisecond -> value;
    };
  }

  /**
   * Same as {@link #formatMilliseconds()} (legacy name).
   */
  public String formatMilliSecond() {
    return formatMilliseconds();
  }

  /**
   * Returns total milliseconds plus the {@code ms} suffix, e.g. {@code "1500ms"}.
   */
  public String formatMilliseconds() {
    return toMilliSecond() + ShortTimeUnit.Millisecond.getSuffix();
  }

  @Override
  public String toString() {
    return value + unit.getSuffix();
  }

  @Override
  public String toHumanString() {
    double v = toSecond();
    if (v < 1) {
      return getFormatString(toMilliSecond(), ShortTimeUnit.Millisecond);
    }
    v = toMinutes();
    if (v < 1) {
      return getFormatString(toSecond(), ShortTimeUnit.Second);
    }
    v = toHours();
    if (v < 1) {
      return getFormatString(toMinutes(), ShortTimeUnit.Minute);
    }
    v = toDays();
    if (v < 1) {
      return getFormatString(toHours(), ShortTimeUnit.Hour);
    }
    return getFormatString(v, ShortTimeUnit.Day);
  }

  public String toHumanString(ShortTimeUnit unit) {
    return switch (unit) {
      case Millisecond -> toMilliSecond() + unit.getSuffix();
      case Second -> toSecond() + unit.getSuffix();
      case Minute -> toMinutes() + unit.getSuffix();
      case Hour -> toHours() + unit.getSuffix();
      case Day -> toDays() + unit.getSuffix();
    };
  }

  private static String getFormatString(double size, ShortTimeUnit unit) {
    BigDecimal bd = new BigDecimal(size);
    double result = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    return result + unit.getSuffix();
  }

  public static TimeValue parse(CharSequence text) {
    return parse(text, null);
  }

  public static TimeValue parse(CharSequence text, ShortTimeUnit defaultUnit) {
    Assert.assertNotNull(text, "Text must not be null");
    try {
      Matcher matcher = PATTERN.matcher(text);
      assertState(matcher.matches(), "Does not match time value pattern");
      long amount = Long.parseLong(matcher.group(1));
      ShortTimeUnit unit = determineTimeUnit(matcher.group(2), defaultUnit);
      return TimeValue.of(amount, unit);
    } catch (Exception ex) {
      throw new IllegalArgumentException("'" + text + "' is not a valid time value", ex);
    }
  }

  private static ShortTimeUnit determineTimeUnit(String suffix, ShortTimeUnit defaultUnit) {
    ShortTimeUnit defaultUnitToUse =
        defaultUnit != null ? defaultUnit : ShortTimeUnit.Millisecond;
    return StringUtils.hasLength(suffix) ? ShortTimeUnit.fromSuffix(suffix) : defaultUnitToUse;
  }

  @Override
  public Long getValue() {
    return value;
  }

  @Override
  public ShortTimeUnit getUnit() {
    return unit;
  }

  @Override
  public int compareTo(TimeValue o) {
    return Long.compare(toMilliSecond(), o.toMilliSecond());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TimeValue timeValue)) {
      return false;
    }
    return toMilliSecond() == timeValue.toMilliSecond();
  }

  @Override
  public int hashCode() {
    return Long.hashCode(toMilliSecond());
  }
}
