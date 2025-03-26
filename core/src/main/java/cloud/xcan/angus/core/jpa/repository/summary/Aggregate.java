package cloud.xcan.angus.core.jpa.repository.summary;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlType.DEFAULT;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class Aggregate {

  @NotNull
  @Schema(description = "Database aggregate function.", requiredMode = RequiredMode.REQUIRED)
  private AggregateFunction function;

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(description = "Database aggregate column name.", requiredMode = RequiredMode.REQUIRED)
  private String column;

  public Aggregate() {
    this.function = AggregateFunction.COUNT;
    this.column = SummaryQueryRegister.DEFAULT_AGGREGATE_COLUMN;
  }

  public String toColumnName() {
    return function.name() + "_" + column;
  }
}
