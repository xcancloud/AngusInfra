package cloud.xcan.angus.remote;

import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_FILTER_COLUMN_LENGTH;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_FILTER_SIZE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractQuery extends CommDto implements Serializable {

  @Length(max = MAX_FILTER_COLUMN_LENGTH)
  @Schema(description = "Sort field")
  private String orderBy;

  @Schema(description = "Sorting method")
  private OrderSort orderSort;

  protected abstract String getDefaultOrderBy();

  protected abstract OrderSort getDefaultOrderSort();

  @Size(max = MAX_FILTER_SIZE)
  @Schema(description = "Filter conditions, Max 20")
  private List<SearchCriteria> filters = new ArrayList<>();

  public boolean containsKey(String key) {
    if (ObjectUtils.isEmpty(filters)) {
      return false;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        return true;
      }
    }
    return false;
  }

  public boolean containsKey(String key, SearchOperation op) {
    if (ObjectUtils.isEmpty(filters)) {
      return false;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        return true;
      }
    }
    return false;
  }

  public List<SearchCriteria> findByKey(String key) {
    List<SearchCriteria> criterias = new ArrayList<>();
    if (ObjectUtils.isEmpty(filters)) {
      return criterias;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        criterias.add(criteria);
      }
    }
    return criterias;
  }

  public List<SearchCriteria> findByKey(String key, SearchOperation op) {
    List<SearchCriteria> criterias = new ArrayList<>();
    if (ObjectUtils.isEmpty(filters)) {
      return criterias;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        criterias.add(criteria);
      }
    }
    return criterias;
  }

  public List<String> findValueByKey(String key, SearchOperation op) {
    if (ObjectUtils.isEmpty(key) || Objects.isNull(op)) {
      return null;
    }
    List<SearchCriteria> criterias = findByKey(key, op);
    if (ObjectUtils.isEmpty(criterias)) {
      return null;
    }
    return criterias.stream().map(c -> safeStringValue(c.getValue().toString()))
        .collect(Collectors.toList());
  }

  public String findFirstValueByKey(String key,
      SearchOperation op) {
    if (ObjectUtils.isEmpty(key) || Objects.isNull(op)) {
      return null;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        return safeStringValue(criteria.getValue().toString());
      }
    }
    return null;
  }

  public List<String> getFilterInValue(String key) {
    List<String> values = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(filters)) {
      List<SearchCriteria> criterias = findByKey(key, SearchOperation.IN);
      if (CollectionUtils.isNotEmpty(criterias)) {
        for (SearchCriteria criteria : criterias) {
          if (Objects.nonNull(criteria.getValue()) && criteria.isValidCriteria()) {
            String sv = safeStringInValue(criteria.getValue().toString());
            values.addAll(Arrays.asList(sv.split(",")));
          }
        }
      }
    }
    return values;
  }

  public String getFilterInFirstValue(String key) {
    if (ObjectUtils.isNotEmpty(filters)) {
      List<SearchCriteria> criterias = findByKey(key, SearchOperation.IN);
      if (CollectionUtils.isNotEmpty(criterias)) {
        return safeStringInValue(criterias.get(0).getValue().toString());
      }
    }
    return null;
  }
}
