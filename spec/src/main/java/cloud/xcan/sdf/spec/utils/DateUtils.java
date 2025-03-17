package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_10;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_10_P;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_4;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_4_P;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_P;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_FORMAT;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * DateUtils provides date formatting, parsing
 */
public abstract class DateUtils extends org.apache.commons.lang3.time.DateUtils {

  /**
   * Patterns
   */
  public static DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter
      .ofPattern(DEFAULT_DATE_TIME_FORMAT);
  public static DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FMT_4);
  public static DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(DATE_FMT_10);

  public static final Date DEFAULT_DATE = DateUtils.parseByDatePattern("1970-01-01");

  /**
   * Parse date by 'yyyy-MM-dd' pattern
   */
  public static Date parseByDatePattern(String str) {
    return parseDate(str, DEFAULT_DATE_FORMAT);
  }

  /**
   * Parse date by 'yyyy-MM-dd HH:mm:ss' pattern
   */
  public static Date parseByDateTimePattern(String str) {
    return parseDate(str, DEFAULT_DATE_TIME_FORMAT);
  }

  /**
   * Parse date without Checked exception
   *
   * @throws RuntimeException when ParseException occurred
   */
  public static Date parseDate(String str, String pattern) {
    try {
      return parseDate(str, new String[]{pattern});
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static LocalDateTime getLocalDateTime(String date) {
    try {
      String stringValue = date.replaceAll("\"", "");
      if (DATE_FMT_P.matcher(stringValue).matches()) {
        return LocalDateTime.parse(stringValue, DATE_TIME_FMT);
      }
    } catch (Exception e) {
      // NOOP
    }
    return null;
  }

  public static LocalDate getLocalDate(String date) {
    try {
      String stringValue = date.replaceAll("\"", "");
      if (DATE_FMT_4_P.matcher(stringValue).matches()) {
        return LocalDate.parse(stringValue, DATE_FMT);
      }
    } catch (Exception e) {
      // NOOP
    }
    return null;
  }

  public static LocalTime getLocalTime(String date) {
    try {
      String stringValue = date.replaceAll("\"", "");
      if (DATE_FMT_10_P.matcher(stringValue).matches()) {
        return LocalTime.parse(stringValue, TIME_FMT);
      }
    } catch (Exception e) {
      // NOOP
    }
    return null;
  }

  /**
   * Format date into string
   */
  public static String formatDate(Date date, String pattern) {
    return DateFormatUtils.format(date, pattern);
  }

  /**
   * Format date by 'yyyy-MM-dd' pattern
   */
  public static String formatByDatePattern(Date date) {
    if (date != null) {
      return DateFormatUtils.format(date, DEFAULT_DATE_FORMAT);
    } else {
      return null;
    }
  }

  /**
   * Format date by 'yyyy-MM-dd HH:mm:ss' pattern
   */
  public static String formatByDateTimePattern(Date date) {
    return DateFormatUtils.format(date, DEFAULT_DATE_TIME_FORMAT);
  }

  /**
   * Get current day using format date by 'yyyy-MM-dd HH:mm:ss' pattern
   *
   * @author yebo
   */
  public static String getCurrentDayByDayPattern() {
    Calendar cal = Calendar.getInstance();
    return formatByDatePattern(cal.getTime());
  }

  public static Date asDate(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Date asDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static LocalDate asLocalDate(Date date) {
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public static LocalDateTime asLocalDateTime(Date date) {
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public static LocalDateTime asLocalDateTime(long timestamp) {
    return asLocalDateTime(timestamp, ZoneId.systemDefault());
  }

  public static LocalDateTime asLocalDateTime(long timestamp, ZoneId zoneId) {
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZonedDateTime dateTime = instant.atZone(zoneId);
    return dateTime.toLocalDateTime();
  }

}
