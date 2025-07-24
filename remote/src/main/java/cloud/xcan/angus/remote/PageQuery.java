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
@Schema(description = """
    PageQuery is a data structure used for paginated queries with flexible filtering, sorting, and search options.

    Usage:
     - Specify pagination (pageNo, pageSize) to control result pages.
     - Use orderBy and orderSort to define sorting field and direction.
     - Add dynamic filter conditions via filters (array of SearchCriteria).
     - Enable fullTextSearch for full-text search (default: false, uses DB index search otherwise).
     - Set infoScope to control the detail level of returned data.""")
public abstract class PageQuery extends AbstractQuery implements Serializable {

  @Min(1)
  @Max(MAX_PAGE_NO)
  @Schema(description = "Page number for paginated data (default: 1)")
  private Integer pageNo = DEFAULT_PAGE_NO;

  @Min(1)
  @Max(MAX_PAGE_SIZE)
  @Schema(description = "Number of items per page (default: 10)")
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
