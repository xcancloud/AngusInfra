package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTH_APP_VERSION;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTH_DEVICE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.USER_AGENT;

import cloud.xcan.angus.api.enums.DeviceType;
import cloud.xcan.angus.api.pojo.DeviceInfo;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.DigestUtils;

public class DeviceInfoExtractorUtils {

  /**
   * 提取设备信息
   */
  public static DeviceInfo extractDeviceInfo(HttpServletRequest request) {
    String userAgent = request.getHeader(USER_AGENT);
    String deviceId = extractDeviceId(request);

    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setUserAgent(userAgent);
    deviceInfo.setDeviceId(deviceId);
    deviceInfo.setDeviceType(parseDeviceType(userAgent));
    deviceInfo.setPlatform(parsePlatform(userAgent));
    deviceInfo.setOsVersion(parseOsVersion(userAgent));
    deviceInfo.setBrowser(parseBrowser(userAgent));
    deviceInfo.setBrowserVersion(parseBrowserVersion(userAgent));
    deviceInfo.setAppVersion(request.getHeader(AUTH_APP_VERSION));
    return deviceInfo;
  }

  /**
   * 提取设备ID（支持多种方式）
   */
  public static String extractDeviceId(HttpServletRequest request) {
    // 1. 从请求头获取
    String deviceId = request.getHeader(AUTH_DEVICE_ID);
    if (StringUtils.hasText(deviceId)) {
      return deviceId;
    }

    // 2. 从自定义header获取
    deviceId = request.getHeader("Device-Id");
    if (StringUtils.hasText(deviceId)) {
      return deviceId;
    }

    // 3. 从参数获取
    deviceId = request.getParameter("deviceId");
    if (StringUtils.hasText(deviceId)) {
      return deviceId;
    }

    // 4. 生成临时设备ID（如果业务允许）
    return generateTemporaryDeviceId(request);
  }

  /**
   * 解析设备类型
   */
  public static DeviceType parseDeviceType(String userAgent) {
    if (userAgent == null) {
      return DeviceType.UNKNOWN;
    }

    userAgent = userAgent.toLowerCase();

    // 移动设备
    if (userAgent.contains("mobile") || userAgent.contains("android") ||
        userAgent.contains("iphone")) {
      return DeviceType.MOBILE;
    }

    // 平板
    if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
      return DeviceType.TABLET;
    }

    // 桌面设备
    if (userAgent.contains("windows nt") || userAgent.contains("mac os") ||
        userAgent.contains("linux")) {
      return DeviceType.DESKTOP;
    }

    // 机器人/爬虫
    if (userAgent.contains("bot") || userAgent.contains("spider") ||
        userAgent.contains("crawler")) {
      return DeviceType.BOT;
    }

    return DeviceType.UNKNOWN;
  }

  /**
   * 解析操作系统/平台
   */
  public static String parsePlatform(String userAgent) {
    if (userAgent == null) {
      return "Unknown";
    }

    userAgent = userAgent.toLowerCase();

    if (userAgent.contains("windows")) {
      return "Windows";
    } else if (userAgent.contains("mac os")) {
      return "macOS";
    } else if (userAgent.contains("linux")) {
      return "Linux";
    } else if (userAgent.contains("android")) {
      return "Android";
    } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
      return "iOS";
    }
    return "Other";
  }

  /**
   * 解析浏览器
   */
  public static String parseBrowser(String userAgent) {
    if (userAgent == null) {
      return "Unknown";
    }

    userAgent = userAgent.toLowerCase();
    if (userAgent.contains("chrome") && !userAgent.contains("edg")) {
      return "Chrome";
    } else if (userAgent.contains("firefox")) {
      return "Firefox";
    } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
      return "Safari";
    } else if (userAgent.contains("edge")) {
      return "Edge";
    } else if (userAgent.contains("opera")) {
      return "Opera";
    }
    return "Other";
  }

  /**
   * 从 User-Agent 中提取浏览器版本号
   *
   * @param userAgent User-Agent 字符串
   * @return 版本号，如 "91.0.4472.124"，未识别则返回 "Unknown"
   */
  public static String parseBrowserVersion(String userAgent) {
    if (userAgent == null) return "Unknown";

    // 优先级：Edge(Edg) > Chrome > Firefox > Safari > Opera > IE
    String[] patterns = {
        "(?:Edg|Edge)/([\\d.]+)",          // Edge (基于 Chromium)
        "Chrome/([\\d.]+)(?!.*Edg)",        // Chrome (排除 Edge)
        "Firefox/([\\d.]+)",
        "Version/([\\d.]+).*Safari",        // Safari 版本通常位于 Version/ 中
        "OPR/([\\d.]+)",                    // Opera
        "MSIE (\\d+[.\\d]*)",                // IE ≤ 10
        "rv:(\\d+[.\\d]*).*Trident"          // IE 11
    };

    for (String pattern : patterns) {
      Matcher m = Pattern.compile(pattern).matcher(userAgent);
      if (m.find()) {
        return m.group(1);
      }
    }

    // 后备：尝试提取常见浏览器的版本（如 Safari 直接标记）
    Matcher m = Pattern.compile("Safari/([\\d.]+)").matcher(userAgent);
    if (m.find() && !userAgent.contains("Chrome")) {
      return m.group(1);  // 注意：Safari 的版本通常是 WebKit 版本，并非实际浏览器版本，但作为后备
    }

    return "Unknown";
  }

  /**
   * 从 User-Agent 中提取操作系统版本号
   *
   * @param userAgent User-Agent 字符串
   * @return 版本号，如 "10.0"、"10_15_7" 会转换为 "10.15.7"，未识别则返回 "Unknown"
   */
  public static String parseOsVersion(String userAgent) {
    if (userAgent == null) return "Unknown";

    String ua = userAgent.toLowerCase();

    // Windows
    Pattern winPattern = Pattern.compile("windows nt (\\d+\\.\\d+)");
    Matcher winMatcher = winPattern.matcher(ua);
    if (winMatcher.find()) {
      return winMatcher.group(1);
    }

    // macOS (Mac OS X 10_15_7 -> 10.15.7)
    Pattern macPattern = Pattern.compile("mac os x (\\d+[._]\\d+(?:[._]\\d+)*)");
    Matcher macMatcher = macPattern.matcher(ua);
    if (macMatcher.find()) {
      return macMatcher.group(1).replace('_', '.');
    }

    // iOS (iPhone OS 14_4 like Mac OS X -> 14.4)
    Pattern iosPattern = Pattern.compile("(?:iphone|ipad|ipod).*?os (\\d+[._]\\d+(?:[._]\\d+)*)");
    Matcher iosMatcher = iosPattern.matcher(ua);
    if (iosMatcher.find()) {
      return iosMatcher.group(1).replace('_', '.');
    }

    // Android
    Pattern androidPattern = Pattern.compile("android (\\d+(?:\\.\\d+)*)");
    Matcher androidMatcher = androidPattern.matcher(ua);
    if (androidMatcher.find()) {
      return androidMatcher.group(1);
    }

    // Linux 通常不携带版本号
    if (ua.contains("linux")) {
      return "Unknown"; // 或返回 "Linux" 但无版本
    }

    return "Unknown";
  }

  // ========== 可选增强：获取完整名称（含版本）==========

  /**
   * 获取完整浏览器标识（名称 + 版本）
   */
  public static String getBrowserFull(String userAgent) {
    String name = parseBrowser(userAgent);
    String version = parseBrowserVersion(userAgent);
    return "Unknown".equals(version) ? name : name + " " + version;
  }

  /**
   * 获取完整操作系统标识（名称 + 版本）
   */
  public static String getPlatformFull(String userAgent) {
    String platform = parsePlatform(userAgent);
    String version = parseOsVersion(userAgent);
    return "Unknown".equals(version) ? platform : platform + " " + version;
  }

  /**
   * 生成临时设备ID（基于会话和IP）
   */
  public static String generateTemporaryDeviceId(HttpServletRequest request) {
    String sessionId = request.getSession().getId();
    String ip = getClientIp(request);
    return DigestUtils.md5DigestAsHex((sessionId + ip).getBytes()).substring(0, 16);
  }

  public static String getClientIp(HttpServletRequest request) {
    // 简化的IP获取，实际项目中应使用更完整的IP获取方法
    return request.getRemoteAddr();
  }
}
