package cloud.xcan.angus.remote;

import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_FILTER_COLUMN_LENGTH;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_FILTER_SIZE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "Query common parameters")
public abstract class AbstractQuery implements Serializable {

  @Length(max = MAX_FILTER_COLUMN_LENGTH)
  @Schema(description = "Specifies the field used as the basis for sorting the data")
  protected String orderBy;

  @Schema(description = "Specifies the direction of the sorting (ascending or descending)")
  protected OrderSort orderSort;

  /**
   * @see SearchCriteria#INFO_SCOPE_KEY
   */
  @Schema(description = "Query information scope", hidden = true)
  public InfoScope infoScope;

  @Schema(description = "Specifies whether to use full-text search, defaulting to false (database index search)")
  public boolean fullTextSearch = false;

  @Size(max = MAX_FILTER_SIZE)
  @Parameter(style = ParameterStyle.FORM, explode = Explode.TRUE, in = ParameterIn.QUERY,
      description = "Dynamic filter and search conditions, max " + MAX_FILTER_SIZE,
      array = @ArraySchema(schema = @Schema(type = "object", implementation = SearchCriteria.class)))
  protected List<SearchCriteria> filters = new ArrayList<>();

  protected abstract String getDefaultOrderBy();

  protected abstract OrderSort getDefaultOrderSort();

  public boolean containsKey(String key) {
    if (isEmpty(filters)) {
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
    if (isEmpty(filters)) {
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
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters)) {
      return criteria;
    }
    for (SearchCriteria criteria0 : filters) {
      if (key.equals(criteria0.getKey())) {
        criteria.add(criteria0);
      }
    }
    return criteria;
  }

  public List<SearchCriteria> findByKey(String key, SearchOperation op) {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters)) {
      return criteria;
    }
    for (SearchCriteria criteria0 : filters) {
      if (key.equals(criteria0.getKey()) && op.equals(criteria0.getOp())) {
        criteria.add(criteria0);
      }
    }
    return criteria;
  }

  public List<String> findValueByKey(String key, SearchOperation op) {
    if (isEmpty(key) || Objects.isNull(op)) {
      return null;
    }
    List<SearchCriteria> criteria = findByKey(key, op);
    if (isEmpty(criteria)) {
      return null;
    }
    return criteria.stream().map(c -> safeStringValue(c.getValue().toString()))
        .collect(Collectors.toList());
  }

  public String findFirstValueByKey(String key,
      SearchOperation op) {
    if (isEmpty(key) || Objects.isNull(op)) {
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
    if (isNotEmpty(filters)) {
      List<SearchCriteria> criteria = findByKey(key, SearchOperation.IN);
      if (isNotEmpty(criteria)) {
        for (SearchCriteria criteria0 : criteria) {
          if (Objects.nonNull(criteria0.getValue()) && criteria0.isValidCriteria()) {
            String sv = safeStringInValue(criteria0.getValue().toString());
            values.addAll(Arrays.asList(sv.split(",")));
          }
        }
      }
    }
    return values;
  }

  public String getFilterInFirstValue(String key) {
    if (isNotEmpty(filters)) {
      List<SearchCriteria> criteria = findByKey(key, SearchOperation.IN);
      if (isNotEmpty(criteria)) {
        return safeStringInValue(criteria.get(0).getValue().toString());
      }
    }
    return null;
  }
}
