package cloud.xcan.sdf.spec.unit;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.spec.utils.StringUtils;
import java.text.DecimalFormat;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RateValue {

  private double value;

  public RateValue() {
  }

  public RateValue(double value) {
    this.value = value;
  }

  public static RateValue parse(String value) {
    if (isEmpty(value)) {
      return new RateValue();
    }

    String value0 = StringUtils.removeSpace(value);
    return value0.endsWith("%")
        ? new RateValue(Double.parseDouble(value0.substring(0, value0.length() - 2)))
        : new RateValue(Double.parseDouble(value0));
  }

  public String toHumanString() {
    return new DecimalFormat("0.00").format(value);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
