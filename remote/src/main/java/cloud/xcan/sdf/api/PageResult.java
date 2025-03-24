package cloud.xcan.sdf.api;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
public class PageResult<T> implements Serializable {

  private static final List<?> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>(0));
  private static final PageResult<?> EMPTY_RESULT = new PageResult<>();

  @Schema(description = "Total number", example = "10")
  private long total;

  @JsonIgnore
  public boolean isEmpty() {
    return total <= 0 && ObjectUtils.isEmpty(list);
  }

  @SuppressWarnings("unchecked")
  @Schema(description = "Page data")
  private List<T> list = (List<T>) EMPTY_LIST;

  @SuppressWarnings("unchecked")
  public static <T> PageResult<T> empty() {
    return (PageResult<T>) EMPTY_RESULT;
  }

  public static <T> PageResult<T> of(long total, List<T> result) {
    PageResult<T> pageResult = new PageResult<T>();
    pageResult.setTotal(total);
    pageResult.setList(result);
    return pageResult;
  }

  public static <T> PageResult<T> of(Page<T> result) {
    PageResult<T> pageResult = new PageResult<T>();
    pageResult.setTotal(result.getTotalElements());
    pageResult.setList(result.toList());
    return pageResult;
  }

}
