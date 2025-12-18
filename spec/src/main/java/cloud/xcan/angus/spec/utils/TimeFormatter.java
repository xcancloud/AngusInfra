package cloud.xcan.angus.spec.utils;

/**
 * 时间格式化工具类 将毫秒值格式化为人类可读的时间字符串，支持多种精度和格式
 */
public class TimeFormatter {

  // 时间单位常量
  private static final long MILLIS_PER_SECOND = 1000;
  private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
  private static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

  /**
   * 格式化精度
   */
  public enum Precision {
    AUTO,       // 自动选择合适的精度
    DAYS,       // 天
    HOURS,      // 时
    MINUTES,    // 分
    SECONDS,    // 秒
    MILLIS      // 毫秒
  }

  /**
   * 格式化样式
   */
  public enum Style {
    COMPACT,    // 紧凑格式，如：1h30m
    SPACED,     // 带空格格式，如：1h 30m
    VERBOSE,    // 详细格式，如：1 hour 30 minutes
    COLON       // 冒号格式，如：01:30:00
  }

  /**
   * 将毫秒值格式化为可读时间字符串（默认设置）
   *
   * @param millis 毫秒值
   * @return 格式化后的时间字符串
   */
  public static String format(long millis) {
    return format(millis, Precision.AUTO, Style.COMPACT);
  }

  /**
   * 将毫秒值格式化为可读时间字符串
   *
   * @param millis    毫秒值
   * @param precision 格式化精度
   * @param style     格式化样式
   * @return 格式化后的时间字符串
   */
  public static String format(long millis, Precision precision, Style style) {
    if (millis < 0) {
      throw new IllegalArgumentException("Time value cannot be negative");
    }

    if (millis == 0) {
      return switch (style) {
        case VERBOSE -> "0 seconds";
        case COLON -> "00:00:00";
        default -> "0s";
      };
    }

    long remaining = millis;

    // 计算各个时间单位
    long days = remaining / MILLIS_PER_DAY;
    remaining %= MILLIS_PER_DAY;

    long hours = remaining / MILLIS_PER_HOUR;
    remaining %= MILLIS_PER_HOUR;

    long minutes = remaining / MILLIS_PER_MINUTE;
    remaining %= MILLIS_PER_MINUTE;

    long seconds = remaining / MILLIS_PER_SECOND;
    remaining %= MILLIS_PER_SECOND;

    // 根据精度确定显示哪些单位
    boolean showDays = days > 0;
    boolean showHours = hours > 0;
    boolean showMinutes = minutes > 0;
    boolean showSeconds = seconds > 0;
    boolean showMillis = remaining > 0;

    // 应用精度设置
    switch (precision) {
      case DAYS:
        // 强制显示天数，即使为0
        showDays = true;
        showHours = showMinutes = showSeconds = showMillis = false;
        break;
      case HOURS:
        // 强制显示小时，将天数转换为小时
        showDays = false;
        showHours = true;
        showMinutes = showSeconds = showMillis = false;
        hours = days * 24 + hours;
        break;
      case MINUTES:
        // 强制显示分钟，将天和小时转换为分钟
        showDays = showHours = false;
        showMinutes = true;
        showSeconds = showMillis = false;
        minutes = days * 24 * 60 + hours * 60 + minutes;
        break;
      case SECONDS:
        // 强制显示秒，将天、小时、分钟转换为秒
        showDays = showHours = showMinutes = false;
        showSeconds = true;
        showMillis = false;
        seconds = days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60 + seconds;
        break;
      case MILLIS:
        // 强制显示毫秒，将所有单位转换为毫秒
        showDays = showHours = showMinutes = showSeconds = false;
        showMillis = true;
        remaining = days * MILLIS_PER_DAY + hours * MILLIS_PER_HOUR 
            + minutes * MILLIS_PER_MINUTE + seconds * MILLIS_PER_SECOND + remaining;
        break;
      case AUTO:
        // 自动模式：从最高非零单位开始，最多显示2个单位
        // 特殊情况：如果有秒和毫秒，显示秒和毫秒（即使秒为0）
        // 如果只有毫秒，只显示毫秒
        boolean[] units = {showDays, showHours, showMinutes, showSeconds, showMillis};
        int firstNonZero = -1;
        int unitCount = 0;

        for (int i = 0; i < units.length; i++) {
          if (units[i]) {
            if (firstNonZero == -1) {
              firstNonZero = i;
            }
            unitCount++;
          }
        }

        if (unitCount > 0) {
          // 特殊情况：如果有秒和毫秒，显示秒和毫秒
          if (firstNonZero == 3 && showMillis) {
            // 有秒和毫秒，显示秒和毫秒
            showSeconds = true;
            showMillis = true;
          } else if (firstNonZero == 4 && unitCount == 1) {
            // 只有毫秒，只显示毫秒（不显示0秒）
            showSeconds = false;
            showMillis = true;
          } else {
            // 只保留第一个非零单位和它后面的一个单位
            for (int i = firstNonZero + 2; i < units.length; i++) {
              units[i] = false;
            }
            showDays = units[0];
            showHours = units[1];
            showMinutes = units[2];
            showSeconds = units[3];
            showMillis = units[4];
          }
        }
        break;
    }

    // 构建格式化字符串
    return buildFormattedString(days, hours, minutes, seconds, remaining,
        showDays, showHours, showMinutes, showSeconds, showMillis,
        style);
  }

  /**
   * 构建格式化字符串
   */
  private static String buildFormattedString(long days, long hours, long minutes,
      long seconds, long millis,
      boolean showDays, boolean showHours,
      boolean showMinutes, boolean showSeconds,
      boolean showMillis, Style style) {
    StringBuilder result = new StringBuilder();

    switch (style) {
      case COMPACT:
        if (showDays) {
          result.append(days).append("d");
        }
        if (showHours) {
          result.append(hours).append("h");
        }
        if (showMinutes) {
          result.append(minutes).append("m");
        }
        if (showSeconds) {
          result.append(seconds).append("s");
        }
        if (showMillis) {
          result.append(millis).append("ms");
        }
        break;

      case SPACED:
        if (showDays) {
          result.append(days).append("d ");
        }
        if (showHours) {
          result.append(hours).append("h ");
        }
        if (showMinutes) {
          result.append(minutes).append("m ");
        }
        if (showSeconds) {
          result.append(seconds).append("s ");
        }
        if (showMillis) {
          result.append(millis).append("ms");
        }
        // 移除末尾多余的空格
        if (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
          result.deleteCharAt(result.length() - 1);
        }
        break;

      case VERBOSE:
        if (showDays) {
          result.append(days).append(dayText(days)).append(" ");
        }
        if (showHours) {
          result.append(hours).append(hourText(hours)).append(" ");
        }
        if (showMinutes) {
          result.append(minutes).append(minuteText(minutes)).append(" ");
        }
        if (showSeconds) {
          result.append(seconds).append(secondText(seconds)).append(" ");
        }
        if (showMillis) {
          result.append(millis).append(millisText(millis));
        }
        // 移除末尾多余的空格
        if (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
          result.deleteCharAt(result.length() - 1);
        }
        break;

      case COLON:
        // COLON格式：根据显示的单位决定格式
        // 如果有小时或天数，显示 HH:MM:SS（将天数转换为小时）
        // 如果只有分钟和秒，显示 MM:SS
        // 如果只有秒，显示 MM:SS（00:SS）
        // 如果只有毫秒，显示 00:00.000
        
        if (showDays || showHours) {
          // 显示小时格式：HH:MM:SS（将天数转换为小时）
          long totalHours = days * 24 + hours;
          result.append(String.format("%02d:%02d:%02d", totalHours, minutes, seconds));
          if (showMillis) {
            result.append(".").append(String.format("%03d", millis));
          }
        } else if (showMinutes || showSeconds) {
          // 显示分钟格式：MM:SS（即使分钟为0也显示）
          result.append(String.format("%02d:%02d", minutes, seconds));
          if (showMillis) {
            result.append(".").append(String.format("%03d", millis));
          }
        } else if (showMillis) {
          // 只有毫秒：00:00.000
          result.append("00:00.").append(String.format("%03d", millis));
        }
        break;
    }

    return result.toString();
  }


  /**
   * 获取天的文本（单数/复数）
   */
  private static String dayText(long days) {
    return days == 1 ? " day" : " days";
  }

  /**
   * 获取小时的文本（单数/复数）
   */
  private static String hourText(long hours) {
    return hours == 1 ? " hour" : " hours";
  }

  /**
   * 获取分钟的文本（单数/复数）
   */
  private static String minuteText(long minutes) {
    return minutes == 1 ? " minute" : " minutes";
  }

  /**
   * 获取秒的文本（单数/复数）
   */
  private static String secondText(long seconds) {
    return seconds == 1 ? " second" : " seconds";
  }

  /**
   * 获取毫秒的文本（单数/复数）
   */
  private static String millisText(long millis) {
    return millis == 1 ? " millisecond" : " milliseconds";
  }

  /**
   * 快速格式化方法：天 时 分 秒
   */
  public static String formatDHMS(long millis) {
    return format(millis, Precision.AUTO, Style.SPACED);
  }

  /**
   * 快速格式化方法：冒号格式 (HH:MM:SS)
   */
  public static String formatColon(long millis) {
    return format(millis, Precision.AUTO, Style.COLON);
  }

  /**
   * 快速格式化方法：详细描述
   */
  public static String formatVerbose(long millis) {
    return format(millis, Precision.AUTO, Style.VERBOSE);
  }

  /**
   * 快速格式化方法：仅显示秒
   */
  public static String formatSeconds(long millis) {
    return format(millis, Precision.SECONDS, Style.COMPACT);
  }

  /**
   * 快速格式化方法：仅显示分钟
   */
  public static String formatMinutes(long millis) {
    return format(millis, Precision.MINUTES, Style.COMPACT);
  }

  /**
   * 将秒值格式化为可读时间字符串
   */
  public static String formatFromSeconds(long seconds) {
    return format(seconds * 1000);
  }

  /**
   * 将分钟值格式化为可读时间字符串
   */
  public static String formatFromMinutes(long minutes) {
    return format(minutes * 60 * 1000);
  }

  /**
   * 将小时值格式化为可读时间字符串
   */
  public static String formatFromHours(long hours) {
    return format(hours * 60 * 60 * 1000);
  }
}
