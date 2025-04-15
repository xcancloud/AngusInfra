package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.core.utils.CoreUtils.exitApp;
import static cloud.xcan.angus.remote.message.ProtocolException.M.MOBILE_FORMAT_ERROR;
import static cloud.xcan.angus.remote.message.ProtocolException.M.MOBILE_FORMAT_ERROR_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_FORMAT_ERROR_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_FORMAT_ERROR_T;
import static cloud.xcan.angus.spec.utils.NetworkUtils.REGEX_IPV4;
import static org.apache.commons.io.IOUtils.toByteArray;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.spec.utils.crypto.MD5Utils;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtils {

  private ValidatorUtils() { /* no instance */ }

  /**
   * <pre>
   *  移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
   *  联通：130、131、132、152、155、156、185、186
   *  电信：133、153、180、189
   *  总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
   * </pre>
   */
  public static final String REGEX_CHINA_MOBILE = "^((1[3|8][0-9])|(14[5|7|9])|(15([0-3]|[5-9]))|(16[5|6])|(17([0-3]|[5-8]))|(19[1|8|9]))\\d{8}$";

  /**
   * ITU-T E.123 Format
   *
   * <pre>
   * The rules and conventions used to print international phone numbers vary
   * significantly around the world, so it’s hard to provide meaningful validation for an
   * international phone number unless you adopt a strict format. Fortunately, there is a simple,
   * industry-standard notation specified by ITU-T E.123. This notation requires that international
   * phone numbers include a leading plus sign (known as the international prefix symbol), and
   * allows only spaces to separate groups of digits. Although the tilde character (~) can appear
   * within a phone number to indicate the existence of an additional dial tone, it has been
   * excluded from this regular expression since it is merely a procedural element (in other words,
   * it is not actually dialed) and is infrequently used. Thanks to the international phone
   * numbering plan (ITU-T E.164), phone numbers cannot contain more than 15 digits. The shortest
   * international phone numbers in use contain seven digits.
   *
   * EPP format: ^\+[0-9]{1,3}\.[0-9]{4,14}(?:x.+)?$
   * EPP-style phone numbers use the format +CCC.NNNNNNNNNNxEEEE, where C is the 1–3 digit country code, N is up to 14 digits, and E is the (optional) extension.
   * </pre>
   */
  public static final String REGEX_MOBILE_ITUTE123 = "^\\+(?:[0-9] ?){6,14}[0-9]$";

  public static final String REGEX_MOBILE = "^(?:[0-9] ?){6,14}[0-9]$";

  public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

  /**
   * Chinese characters (1-9 Chinese characters) {1,9} SimpleSource interval
   */
  public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5]{1,9}$";

  public static final String REGEX_CHINA_ID_CARD = "(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])";

  //  /**
  //   * ^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]
  //   */
  //  public static final String REGEX_URL = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
  private static final Pattern REGEX_URL = Pattern
      .compile("(?i)^([a-z](?:[-a-z0-9\\+\\.])*)" + // protocol
          ":(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?"
          + // auth
          "((?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*))"
          + // host/ip
          "(?::([0-9]*))?" + // port
          "(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}|\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?$"
      );

  public static boolean isMobile(String mobile) {
    return Pattern.matches(REGEX_MOBILE, mobile);
  }

  public static void checkMobile(String mobile) {
    ProtocolAssert.assertTrue(Pattern.matches(REGEX_MOBILE, mobile), MOBILE_FORMAT_ERROR,
        MOBILE_FORMAT_ERROR_KEY, new Object[]{mobile});
  }

  public static boolean isItute123Mobile(String mobile) {
    return Pattern.matches(REGEX_MOBILE_ITUTE123, mobile);
  }

  public static boolean isChinaMobile(String mobile) {
    return Pattern.matches(REGEX_CHINA_MOBILE, mobile);
  }

  public static void checkChinaMobile(String mobile) {
    ProtocolAssert.assertTrue(Pattern.matches(REGEX_CHINA_MOBILE, mobile), MOBILE_FORMAT_ERROR,
        MOBILE_FORMAT_ERROR_KEY, new Object[]{mobile});
  }

  public static boolean isMobile(String code, String mobile) {
    for (MobileExp exp : MobileExp.values()) {
      if (exp.getCode().equals(code)) {
        if (mobile.startsWith("+")) {
          mobile = mobile.substring(1, mobile.length() - 1);
        }
        Pattern pattern = Pattern.compile(exp.getExp());
        Matcher matcher = pattern.matcher(mobile);
        if (matcher.matches()) {
          return true;
        }
      }
    }
    return false;
  }

  public static void checkMobile(String code, String mobile) {
    boolean res = false;
    for (MobileExp exp : MobileExp.values()) {
      if (exp.getCode().equals(code)) {
        if (mobile.startsWith("+")) {
          mobile = mobile.substring(1, mobile.length() - 1);
        }
        Pattern pattern = Pattern.compile(exp.getExp());
        Matcher matcher = pattern.matcher(mobile);
        if (matcher.matches()) {
          res = true;
          break;
        }
      }
    }
    ProtocolAssert.assertTrue(res, MOBILE_FORMAT_ERROR, MOBILE_FORMAT_ERROR_KEY,
        new Object[]{mobile});
  }

  public static boolean isEmail(String email) {
    return Pattern.matches(REGEX_EMAIL, email);
  }

  public static void checkEmail(String email) {
    ProtocolAssert.assertTrue(Pattern.matches(REGEX_EMAIL, email), PARAM_FORMAT_ERROR_T,
        PARAM_FORMAT_ERROR_KEY, new Object[]{email, "email"});
  }

  public static boolean isChinese(String chinese) {
    return Pattern.matches(REGEX_CHINESE, chinese);
  }

  public static void checkChinese(String chinese) {
    ProtocolAssert.assertTrue(Pattern.matches(REGEX_CHINESE, chinese), PARAM_FORMAT_ERROR_T,
        PARAM_FORMAT_ERROR_KEY, new Object[]{chinese, "chinese"});
  }

  public static boolean isChinaIdCard(String idCard) {
    return Pattern.matches(REGEX_CHINA_ID_CARD, idCard);
  }

  public static void checkChinaIdCard(String idCard) {
    ProtocolAssert.assertTrue(Pattern.matches(REGEX_CHINA_ID_CARD, idCard), PARAM_FORMAT_ERROR_T,
        PARAM_FORMAT_ERROR_KEY, new Object[]{idCard, "idCard"});
  }

  public static boolean isUrl(String url) {
    return REGEX_URL.matcher(url).matches();
  }

  public static void checkUrl(String url) {
    ProtocolAssert.assertTrue(REGEX_URL.matcher(url).matches(), PARAM_FORMAT_ERROR_T,
        PARAM_FORMAT_ERROR_KEY, new Object[]{url, "url"});
  }

  public static void checkIpAddress(String ipAddress) {
    ProtocolAssert.assertTrue(REGEX_IPV4.matcher(ipAddress).matches(), PARAM_FORMAT_ERROR_T,
        PARAM_FORMAT_ERROR_KEY, new Object[]{ipAddress, "ip"});
  }

  public static void checkValidParams(Object param) {
    try {
      String editionType = SpringContextHolder.getCtx().getEnvironment()
          .getProperty(new Str0(
              new long[]{0x479F8F16B670118AL, 0xB2020DACAB3E6EB7L, 0xEBA7DA6FEBFCC7CDL,
                  0x8A20BDAF8BF690FFL}).toString() /* => "info.app.editionType" */);
      if (EditionType.valueOf(editionType).isPrivatization()) {
        String sign = MD5Utils.encrypt(toByteArray(ValidatorUtils.class.getResourceAsStream(
            new Str0(new long[]{0x72EF4DCB15D8F8A3L, 0x69120DB47A85384EL, 0xCFE552E6CDD22372L,
                0x7808D39035915283L}).toString() /* => "LicenseProtector.class" */)));
        boolean valid = Objects.nonNull(sign) && sign.equalsIgnoreCase(new Str0(
            new long[]{0x1BB0D8BA6FC3B55CL, 0xB0B813217510F001L, 0xD156226D7C1EED01L,
                0xA7B547358FDC82C8L, 0xF790BBED26A59110L})
            .toString() /* => "7098161456bd2ac2fe3557feedca00e4" */);
        if (!valid) {
          System.out.println(new Str0(
              new long[]{0x5AC407D52C14D3DEL, 0x3A454D980CCE9F77L, 0x670F88D35256D1L,
                  0xF1CD6A529DAF54CBL, 0xB15385C61F508FB7L, 0x96A553A8A8DFA7L, 0xED2231FFDE35F748L,
                  0x3961D896CE4C1D09L, 0xAE514955B44B11FDL, 0x7DB46AC123D01164L,
                  0xDB6ECDE88CF710CEL})
              .toString() /* => "Critical warning, license signature verification error, system forced exit" */);
          exitApp();
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.out.println(new Str0(
          new long[]{0x5AC407D52C14D3DEL, 0x3A454D980CCE9F77L, 0x670F88D35256D1L,
              0xF1CD6A529DAF54CBL, 0xB15385C61F508FB7L, 0x96A553A8A8DFA7L, 0xED2231FFDE35F748L,
              0x3961D896CE4C1D09L, 0xAE514955B44B11FDL, 0x7DB46AC123D01164L,
              0xDB6ECDE88CF710CEL})
          .toString() /* => "Critical warning, license signature verification error, system forced exit" */);
      exitApp();
    }
  }
}
