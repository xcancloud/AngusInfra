package cloud.xcan.angus.core.spring.service;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.pojo.LocationInfo;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.JsonUtils;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * 可使用本地 IP 库（如 ip2region）或调用第三方 API。
 */
@Slf4j
public final class LocationService {

  private LocationService() {
  }

  /**
   * 方法1：使用本地IP库（推荐，性能好）
   */
  public static LocationInfo getLocationByIp(String ipAddress) {
    LocationInfo location = new LocationInfo();
    if (isEmpty(ipAddress)) {
      return location;
    }
    location.setIp(ipAddress);
    try {
      // 使用 ip2region 等本地库查询
      // String region = searcher.search(ipAddress);
      // 解析 region 字符串获取国家、省份、城市
    } catch (Exception e) {
      log.error("IP地址查询失败: {}", ipAddress, e);
    }
    return location;
  }

  /**
   * 方法2：调用第三方API（需要网络，可能有次数限制）
   */
  public static LocationInfo getLocationFromApi(String ipAddress, SupportedLanguage language) {
    LocationInfo empty = new LocationInfo();
    if (isEmpty(ipAddress) || language == null) {
      return empty;
    }
    empty.setIp(ipAddress);
    try {
      String url = "http://ip-api.com/json/" + ipAddress + "?lang=" + language.getValue();
      HttpSender sender = new HttpUrlConnectionSender(Duration.ofSeconds(3),
          Duration.ofSeconds(10));
      Response response = sender.get(url).send();
      String body = response.body();
      if (response.isSuccessful() && body != null && !body.isBlank()) {
        LocationInfo parsed = JsonUtils.fromJson(body, LocationInfo.class);
        if (parsed != null) {
          if (parsed.getIp() == null) {
            parsed.setIp(ipAddress);
          }
          return parsed;
        }
      }
    } catch (IOException e) {
      log.warn("获取IP地理位置失败: {}", ipAddress, e);
    }
    return empty;
  }
}
