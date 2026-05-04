package cloud.xcan.angus.core.utils;

import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public final class IpAddressUtil {

  private IpAddressUtil() {
  }

  private static final Pattern IPV4_PATTERN = Pattern.compile(
      "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
          + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

  /**
   * Full IPv6 form only; compressed forms are not matched.
   */
  private static final Pattern IPV6_FULL_PATTERN = Pattern.compile(
      "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

  private static final String[] IP_HEADERS = {
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR"
  };

  /**
   * 获取客户端真实IP地址
   */
  public static String getClientIpAddress(HttpServletRequest request) {
    for (String header : IP_HEADERS) {
      String ip = request.getHeader(header);
      if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
        // 多个IP时取第一个
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        if (isValidIp(ip)) {
          return ip;
        }
      }
    }
    // 如果以上都没有获取到，使用request.getRemoteAddr()
    return request.getRemoteAddr();
  }

  /**
   * 验证 IP 文本：IPv4、完整形式的 IPv6，或 IPv6 回环 {@code ::1} 的展开写法。
   */
  public static boolean isValidIp(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }
    return IPV4_PATTERN.matcher(ip).matches()
        || IPV6_FULL_PATTERN.matcher(ip).matches()
        || "0:0:0:0:0:0:0:1".equals(ip);
  }

  /**
   * 检查是否为内部IP地址
   */
  public static boolean isInternalIp(String ip) {
    if (ip == null) {
      return false;
    }

    // 本地地址
    if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("localhost")) {
      return true;
    }

    if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
      return true;
    }
    if (ip.startsWith("172.")) {
      int dot = ip.indexOf('.', 4);
      if (dot > 4) {
        try {
          int second = Integer.parseInt(ip.substring(4, dot));
          return second >= 16 && second <= 31;
        } catch (NumberFormatException ignored) {
          return false;
        }
      }
    }
    return false;
  }
}
