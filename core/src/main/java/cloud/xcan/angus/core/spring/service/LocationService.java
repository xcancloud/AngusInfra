package cloud.xcan.angus.core.spring.service;

import cloud.xcan.angus.api.pojo.LocationInfo;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.JsonUtils;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * 可以使用本地IP库（如ip2region）或调用第三方API
 */
@Slf4j
public class LocationService {

  /**
   * 方法1：使用本地IP库（推荐，性能好）
   */
  public static LocationInfo getLocationByIp(String ipAddress) {
    LocationInfo location = new LocationInfo();

    try {
      // 使用ip2region等本地库查询
      // String region = searcher.search(ipAddress);
      // 解析region字符串获取国家、省份、城市

      // 这里只是示例，实际需要集成ip2region
      location.setIp(ipAddress);

      // location.setCountry("中国");
      // location.setProvince("北京");
      // location.setCity("北京");

    } catch (Exception e) {
      log.error("IP地址查询失败: {}", ipAddress, e);
    }
    return location;
  }

  /**
   * 方法2：调用第三方API（需要网络，可能有次数限制）
   */
  public static LocationInfo getLocationFromApi(String ipAddress, SupportedLanguage language) {
    try {
      // 示例：调用ip-api.com（免费，有频率限制）
      String url = "http://ip-api.com/json/" + ipAddress + "?lang=" + language.getValue();
      HttpSender sender = new HttpUrlConnectionSender(Duration.ofSeconds(3),
          Duration.ofSeconds(10));
      Response response = sender.get(url).send();
      String body = response.body();
      if (response.isSuccessful()) {
        return JsonUtils.fromJson(body, LocationInfo.class);
      }
    } catch (Throwable e) {
      log.warn("获取IP地理位置失败: {}", ipAddress, e);
    }
    return new LocationInfo();
  }
}
