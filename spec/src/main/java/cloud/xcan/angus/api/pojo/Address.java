package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_COUNTRY_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class Address {

  @Length(max = MAX_COUNTRY_LENGTH)
  @Schema(example = "86", maxLength = MAX_COUNTRY_LENGTH)
  private String countryCode;

  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(example = "China", maxLength = DEFAULT_NAME_LENGTH)
  private String country;

  @Length(max = DEFAULT_CODE_LENGTH)
  @Schema(example = "110100", maxLength = DEFAULT_CODE_LENGTH)
  private String provinceCode;

  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(example = "Beijing", maxLength = DEFAULT_NAME_LENGTH)
  private String province;

  @Length(max = DEFAULT_CODE_LENGTH)
  @Schema(example = "110100", maxLength = DEFAULT_CODE_LENGTH)
  private String cityCode;

  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(example = "Beijing", maxLength = DEFAULT_NAME_LENGTH)
  private String city;

  @Length(max = DEFAULT_CODE_LENGTH)
  @Schema(example = "110101", maxLength = DEFAULT_CODE_LENGTH)
  private String areaCode;

  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(example = "Dongcheng", maxLength = DEFAULT_NAME_LENGTH)
  private String area;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @Schema(example = "Tiananmen Square", maxLength = DEFAULT_NAME_LENGTH_X2)
  private String street;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Address address)) {
      return false;
    }
    return Objects.equals(countryCode, address.countryCode) &&
        Objects.equals(country, address.country) &&
        Objects.equals(provinceCode, address.provinceCode) &&
        Objects.equals(province, address.province) &&
        Objects.equals(cityCode, address.cityCode) &&
        Objects.equals(city, address.city) &&
        Objects.equals(areaCode, address.areaCode) &&
        Objects.equals(area, address.area) &&
        Objects.equals(street, address.street);
  }

  @Override
  public int hashCode() {
    return Objects.hash(countryCode, country, provinceCode, province, cityCode, city,
        areaCode, area, street);
  }
}
