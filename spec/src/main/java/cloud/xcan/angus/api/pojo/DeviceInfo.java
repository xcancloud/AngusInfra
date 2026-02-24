package cloud.xcan.angus.api.pojo;

import cloud.xcan.angus.api.enums.DeviceType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class DeviceInfo {

  private String deviceId;
  private DeviceType deviceType; // MOBILE, TABLET, DESKTOP, etc.
  private String platform;   // Android, iOS, Windows, etc.
  private String osVersion;
  private String browser;    // Chrome, Firefox, Safari, etc.
  private String browserVersion;
  private String userAgent;
  private String appVersion;
  private String brand;      // 设备品牌
  private String model;      // 设备型号
}
