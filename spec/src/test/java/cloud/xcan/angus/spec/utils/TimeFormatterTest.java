package cloud.xcan.angus.spec.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * TimeFormatter 单元测试
 */
class TimeFormatterTest {

  @Test
  void testZeroMillis() {
    assertEquals("0s", TimeFormatter.format(0));
    assertEquals("0 seconds", TimeFormatter.formatVerbose(0));
    assertEquals("00:00:00", TimeFormatter.formatColon(0));
  }

  @Test
  void testNegativeValue() {
    assertThrows(IllegalArgumentException.class, () -> {
      TimeFormatter.format(-1000);
    });
  }

  @Test
  void testCompactFormat() {
    // 测试秒
    assertEquals("30s", TimeFormatter.format(30000));
    assertEquals("1m30s", TimeFormatter.format(90000));

    // 测试分钟
    assertEquals("15m", TimeFormatter.format(900000));
    assertEquals("15m30s", TimeFormatter.format(930000));

    // 测试小时
    assertEquals("1h", TimeFormatter.format(3600000));
    assertEquals("1h30m", TimeFormatter.format(5400000));
    assertEquals("1h30m", TimeFormatter.format(5445000));

    // 测试天
    assertEquals("1d", TimeFormatter.format(86400000));
    assertEquals("1d12h", TimeFormatter.format(129600000));
    assertEquals("2d3h", TimeFormatter.format(185100000));
  }

  @Test
  void testSpacedFormat() {
    assertEquals("1h 30m", TimeFormatter.formatDHMS(5400000));
    assertEquals("15m 30s", TimeFormatter.formatDHMS(930000));
    assertEquals("2d 3h", TimeFormatter.formatDHMS(185100000));
    assertEquals("1d 12h", TimeFormatter.formatDHMS(129600000));
  }

  @Test
  void testVerboseFormat() {
    assertEquals("30 seconds", TimeFormatter.formatVerbose(30000));
    assertEquals("1 minute 30 seconds", TimeFormatter.formatVerbose(90000));
    assertEquals("1 hour 30 minutes", TimeFormatter.formatVerbose(5400000));
    assertEquals("2 days 3 hours", TimeFormatter.formatVerbose(185100000));
    assertEquals("1 day 12 hours", TimeFormatter.formatVerbose(129600000));
  }

  @Test
  void testColonFormat() {
    assertEquals("00:30", TimeFormatter.formatColon(30000));
    assertEquals("01:30", TimeFormatter.formatColon(90000));
    assertEquals("01:30:00", TimeFormatter.formatColon(5400000));
    assertEquals("51:25:00", TimeFormatter.formatColon(185100000));
    assertEquals("36:00:00", TimeFormatter.formatColon(129600000));
    assertEquals("00:00.500", TimeFormatter.formatColon(500));
  }

  @Test
  void testPrecisionDays() {
    assertEquals("1d", TimeFormatter.format(129600000,
        TimeFormatter.Precision.DAYS, TimeFormatter.Style.COMPACT));
    assertEquals("1 day", TimeFormatter.format(129600000,
        TimeFormatter.Precision.DAYS, TimeFormatter.Style.VERBOSE));
  }

  @Test
  void testPrecisionHours() {
    assertEquals("36h", TimeFormatter.format(129600000,
        TimeFormatter.Precision.HOURS, TimeFormatter.Style.COMPACT));
    assertEquals("36 hours", TimeFormatter.format(129600000,
        TimeFormatter.Precision.HOURS, TimeFormatter.Style.VERBOSE));
  }

  @Test
  void testPrecisionMinutes() {
    assertEquals("2160m", TimeFormatter.format(129600000,
        TimeFormatter.Precision.MINUTES, TimeFormatter.Style.COMPACT));
    assertEquals("15m", TimeFormatter.format(900000,
        TimeFormatter.Precision.MINUTES, TimeFormatter.Style.COMPACT));
  }

  @Test
  void testPrecisionSeconds() {
    assertEquals("129600s", TimeFormatter.format(129600000,
        TimeFormatter.Precision.SECONDS, TimeFormatter.Style.COMPACT));
    assertEquals("30s", TimeFormatter.format(30000,
        TimeFormatter.Precision.SECONDS, TimeFormatter.Style.COMPACT));
  }

  @Test
  void testPrecisionMillis() {
    assertEquals("500ms", TimeFormatter.format(500,
        TimeFormatter.Precision.MILLIS, TimeFormatter.Style.COMPACT));
    assertEquals("129600000ms", TimeFormatter.format(129600000,
        TimeFormatter.Precision.MILLIS, TimeFormatter.Style.COMPACT));
  }

  @Test
  void testAutoPrecision() {
    // 自动模式应该最多显示2个单位
    assertEquals("1h30m", TimeFormatter.format(5400000));
    assertEquals("15m30s", TimeFormatter.format(930000));
    assertEquals("1d12h", TimeFormatter.format(129600000));
    assertEquals("2d3h", TimeFormatter.format(185100000));
    assertEquals("30s", TimeFormatter.format(30000));
    assertEquals("500ms", TimeFormatter.format(500));
  }

  @Test
  void testFormatFromSeconds() {
    assertEquals("1h", TimeFormatter.formatFromSeconds(3600));
    assertEquals("1m30s", TimeFormatter.formatFromSeconds(90));
  }

  @Test
  void testFormatFromMinutes() {
    assertEquals("1h30m", TimeFormatter.formatFromMinutes(90));
    assertEquals("15m", TimeFormatter.formatFromMinutes(15));
  }

  @Test
  void testFormatFromHours() {
    assertEquals("1d12h", TimeFormatter.formatFromHours(36));
    assertEquals("2h", TimeFormatter.formatFromHours(2));
  }

  @Test
  void testEdgeCases() {
    // 正好1分钟
    assertEquals("1m", TimeFormatter.format(60000));
    assertEquals("1 minute", TimeFormatter.formatVerbose(60000));

    // 正好1小时
    assertEquals("1h", TimeFormatter.format(3600000));
    assertEquals("1 hour", TimeFormatter.formatVerbose(3600000));

    // 正好1天
    assertEquals("1d", TimeFormatter.format(86400000));
    assertEquals("1 day", TimeFormatter.formatVerbose(86400000));

    // 大数值
    assertEquals("3d", TimeFormatter.format(259200000));
    assertEquals("3 days", TimeFormatter.formatVerbose(259200000));
  }

  @Test
  void testMixedUnits() {
    // 测试混合单位的正确舍入
    assertEquals("1h15m", TimeFormatter.format(4500000)); // 1小时15分钟
    assertEquals("2h30m", TimeFormatter.format(9000000)); // 2小时30分钟
    assertEquals("1d6h", TimeFormatter.format(108000000)); // 1天6小时
  }

  @Test
  void testMillisecondPrecision() {
    assertEquals("1s500ms", TimeFormatter.format(1500,
        TimeFormatter.Precision.AUTO, TimeFormatter.Style.COMPACT));
    // 只有毫秒时，AUTO模式只显示毫秒（不显示0秒）
    assertEquals("500ms", TimeFormatter.format(500,
        TimeFormatter.Precision.AUTO, TimeFormatter.Style.COMPACT));
    assertEquals("500ms", TimeFormatter.format(500));
  }
}
