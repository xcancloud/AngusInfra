package cloud.xcan.angus.core.utils;

import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public class IpAddressUtil {

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
   * 验证IP地址格式
   */
  public static boolean isValidIp(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }

    // IPv4验证
    String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    // IPv6简化验证
    String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";

    return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern) || "0:0:0:0:0:0:0:1".equals(ip);
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

    // 内网地址段
    return ip.startsWith("192.168.") || ip.startsWith("10.") ||
        ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
        ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
        ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
        ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
        ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
        ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
        ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
        ip.startsWith("172.30.") || ip.startsWith("172.31.");
  }
}
