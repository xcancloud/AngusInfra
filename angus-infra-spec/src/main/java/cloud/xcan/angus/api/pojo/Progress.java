package cloud.xcan.angus.api.pojo;

import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class Progress {

  private long total;

  private long completed;
  private BigDecimal completedRate;

  public BigDecimal getCompletedRate() {
    return nonNull(completedRate) ? completedRate : total > 0
        ? BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4,
            RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
        .setScale(2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;
  }
}
