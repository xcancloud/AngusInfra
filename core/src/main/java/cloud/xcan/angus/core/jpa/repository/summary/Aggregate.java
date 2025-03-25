package cloud.xcan.angus.core.jpa.repository.summary;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class Aggregate {

  @NotNull
  private AggregateFunction function;

  @Length(max = 80)
  @NotEmpty
  private String column;

  public Aggregate() {
    this.function = AggregateFunction.COUNT;
    this.column = SummaryQueryRegister.DEFAULT_AGGREGATE_COLUMN;
  }

  public String toColumnName() {
    return function.name() + "_" + column;
  }
}
