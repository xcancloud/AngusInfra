package cloud.xcan.angus.remote;

import static cloud.xcan.angus.remote.ApiConstant.DEFAULT_ORDER_BY;
import static cloud.xcan.angus.remote.ApiConstant.DEFAULT_ORDER_SORT;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.DEFAULT_PAGE_NO;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.DEFAULT_PAGE_SIZE;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_PAGE_NO;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_PAGE_SIZE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Pagination query parameters")
public abstract class PageQuery extends AbstractQuery implements Serializable {

  @Min(1)
  @Max(MAX_PAGE_NO)
  @Schema(description = "Number of pages for paginated data, default query is the first page")
  private Integer pageNo = DEFAULT_PAGE_NO;

  @Min(1)
  @Max(MAX_PAGE_SIZE)
  @Schema(description = "The number of items to query per page, default query is the  10 items")
  private Integer pageSize = DEFAULT_PAGE_SIZE;

  @Override
  @JsonIgnore
  @Schema(hidden = true)
  public String getDefaultOrderBy() {
    return DEFAULT_ORDER_BY;
  }

  @Override
  @JsonIgnore
  @Schema(hidden = true)
  public OrderSort getDefaultOrderSort() {
    return DEFAULT_ORDER_SORT;
  }

  public PageRequest tranPage() {
    if (isBlank(this.getDefaultOrderBy())) {
      throw new IllegalArgumentException("Default orderBy not specified");
    }
    String orderBy = isBlank(this.getOrderBy()) ? this.getDefaultOrderBy() : this.getOrderBy();
    OrderSort orderSort = isNull(this.getOrderSort())
        ? this.getDefaultOrderSort() : this.getOrderSort();
    return PageRequest.of(
        nullSafe(pageNo, DEFAULT_PAGE_NO) - 1, nullSafe(pageSize, DEFAULT_PAGE_SIZE),
        Sort.by(new Order(Direction.fromString(orderSort.name()), orderBy)));
  }
}
