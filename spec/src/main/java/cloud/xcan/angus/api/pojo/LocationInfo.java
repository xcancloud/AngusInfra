package cloud.xcan.angus.api.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class LocationInfo {

  private String ip;
  private String country;
  private String countryCode;
  private String region;
  private String regionName;
  private String city;
  private String isp;
  private Double lat; // latitude
  private Double lon; // longitude
  private String timezone;
}
