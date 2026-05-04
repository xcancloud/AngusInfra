package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTH_APP_VERSION;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTH_DEVICE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.USER_AGENT;

import cloud.xcan.angus.api.enums.DeviceType;
import cloud.xcan.angus.api.pojo.DeviceInfo;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
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
    deviceInfo.setBrowser(parseBrowser(userAgent));
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
   * 生成临时设备ID（基于会话和IP）
   */
  public static String generateTemporaryDeviceId(HttpServletRequest request) {
    String sessionId = request.getSession().getId();
    String ip = getClientIp(request);
    return DigestUtils.md5DigestAsHex((sessionId + ip).getBytes()).substring(0, 16);
  }

  public static String getClientIp(HttpServletRequest request) {
    return IpAddressUtil.getClientIpAddress(request);
  }
}
