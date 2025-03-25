package cloud.xcan.angus.spec;

import cloud.xcan.angus.api.obf.Str0;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public interface SpecConstant {

  String APP_NOT_READY = "The application is starting up";

  /**
   * Newline
   */
  String NEW_LINE = "\n";

  long NOOP_LONG = -1L;
  double NOOP_DOUBLE = -1d;
  int NOOP_INT = -1;

  String HTTP1_1 = "HTTP/1.1";

  char CR = '\r';

  String COLON_SPACE = ": ";

  /**
   * Default encoding: UTF-8
   */
  String DEFAULT_ENCODING = "UTF-8";
  Charset UTF8 = Charset.forName(DEFAULT_ENCODING);

  String COMMA_SEPARATOR = ",";

  /**
   * White space character that match Pattern.compile("\\s")
   */
  char CHAR_SPACE = ' ';
  char CHAR_TAB = '\t';
  /**
   * For unit LF
   */
  char CHAR_NEW_LINE = '\n';
  char CHAR_VERTICAL_TAB = '\u000b';
  char CHAR_CARRIAGE_RETURN = '\r';
  char CHAR_FORM_FEED = '\f';
  /**
   * For windows CRLF
   */
  String CHAR_CRLF = "\r\n";

  int DEFAULT_CHUNK_SIZE = 4096;

  String WIN_BASH_EXTENSION = ".bat";
  String LINUX_BASH_EXTENSION = ".sh";

  /**
   * Locale key
   */
  String LOCALE = new Str0(new long[]{0x6C262EE486783F16L, 0x68C0FF44905CACB5L})
      .toString() /* => "locale" */;
  /**
   * Default locale: zh-CN, allowable values: zh-CN,en
   */
  Locale DEFAULT_LOCALE = Locale.CHINA;
  /**
   * TimeZone key
   */
  String TIME_ZONE = new Str0(new long[]{0xE1A0364516F71632L, 0xD60328378C6DC7D3L})
      .toString() /* => "timeZone" */;
  /**
   * Default TimeZone
   */
  String DEFAULT_TIME_ZONE_S = new Str0(
      new long[]{0xEF7F40065BBCD1F1L, 0x961633C3D0FC9D81L, 0xA20758D812AC42E5L})
      .toString() /* => "Asia/Shanghai" */;
  TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_TIME_ZONE_S);
  /**
   * Locale cookie name
   */
  String LOCALE_COOKIE_NAME = new Str0(
      new long[]{0x3FCA0F96EE6E3BCEL, 0x70482707CAD20CFL, 0xD663D1E32E4C72D6L})
      .toString() /* => "localeCookie" */;

  interface DateFormat {

    String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    String DEFAULT_DATE_TIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    String DEFAULT_YEAR_FORMAT = "yyyy";
    String DEFAULT_MONTH_FORMAT = "yyyy-MM";
    String DEFAULT_WEEK_FORMAT = "yyyy-w";
    String DEFAULT_DAY_FORMAT = DEFAULT_DATE_FORMAT;
    String DEFAULT_HOUR_FORMAT = "yyyy-MM-dd HH";

    String DATE_FMT = DEFAULT_DATE_TIME_FORMAT;
    String DEFAULT_DATA_FORMAT = "yyyy/MM/dd";
    String DATE_FMT_1 = DEFAULT_DATA_FORMAT;
    String DATE_FMT_0 = "yyyyMMdd";
    String DATE_FMT_2 = "yyyy/MM/dd hh:mm:ss";
    String DATE_FMT_3 = "yyyy/MM/dd hh:mm";
    String DATE_FMT_4 = "yyyy-MM-dd";
    String DATE_FMT_5 = "yyyyMMddHHmmss";
    String DATE_FMT_6 = "dd/MM/yy HH:mm:ss";
    String DATE_FMT_7 = "YYYY-MM-DD HH24:MI:SS";
    String DATE_FMT_8 = "dd/MMM/yyyy:HH:mm:ss +0900";
    String DATE_FMT_9 = "dd/MM/yyyy:HH:mm:ss +0900";
    String DATE_FMT_10 = "HH:mm:ss";
    String DATE_FMT_11 = "yyyyMMddHHmmssSSS";
    String DATE_FMT_12 = "MM/dd hh:mm:ss";

    Pattern DATE_FMT_P = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$");
    Pattern DATE_FMT_4_P = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})$");
    Pattern DATE_FMT_10_P = Pattern.compile("^(\\d{2}:\\d{2}:\\d{2})$");
  }

  interface TimeMillis {

    long DAY_MILLI = 24 * 60 * 60 * 1000;
    long HOUR_MILLI = 60 * 60 * 1000;
    long MINUTE_MILLI = 60 * 1000;
    long SECOND_MILLI = 1000;
  }

}
