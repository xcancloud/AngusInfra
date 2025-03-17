package cloud.xcan.sdf.api.search;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.validator.StringEnums;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria implements Serializable {

  public static final String FILERS_KEY = "filters";
  public static final String PAGE_NO_KEY = "pageNo";
  public static final String PAGE_SIZE_KEY = "pageSize";
  public static final String INFO_SCOPE_KEY = "infoScope";

  public static final List<String> IGNORE_FIELDS = List
      .of(FILERS_KEY, PAGE_NO_KEY, PAGE_SIZE_KEY, INFO_SCOPE_KEY);

  /**
   * Filter field
   */
  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(description = "Filter field")
  private String key;

  /**
   * Field value
   */
  @Schema(description = "Filter value")
  private Object value;

  /**
   * Comparison condition
   */
  @StringEnums(enumClass = SearchOperation.class)
  @Schema(description = "Filter condition")
  private SearchOperation op;

  private SearchCriteria(Builder builder) {
    setKey(builder.key);
    setValue(builder.value);
    setOp(builder.op);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isIgnoreFields() {
    return IGNORE_FIELDS.contains(key);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isValidCriteria() {
    return SearchOperation.isValidCriteria(this);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNotValidCriteria() {
    return !SearchOperation.isValidCriteria(this);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean keyEqual(String paramKey) {
    return nonNull(paramKey) && paramKey.equalsIgnoreCase(key);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isRangeSearch() {
    return op.equals(SearchOperation.GREATER_THAN) || op.equals(SearchOperation.LESS_THAN) || op
        .equals(SearchOperation.GREATER_THAN_EQUAL) || op.equals(SearchOperation.LESS_THAN_EQUAL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isMatchSearch() {
    return op.equals(SearchOperation.MATCH) || op.equals(SearchOperation.MATCH_END);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNotMatchSearch() {
    return op.equals(SearchOperation.NOT_MATCH) || op.equals(SearchOperation.NOT_MATCH_END);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isInSearch() {
    return op.equals(SearchOperation.IN) || op.equals(SearchOperation.NOT_IN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isInOrNotSearch() {
    return op.equals(SearchOperation.NOT_EQUAL) || op.equals(SearchOperation.IN) || op
        .equals(SearchOperation.NOT_IN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isEqual() {
    return op.equals(SearchOperation.EQUAL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNotEqual() {
    return op.equals(SearchOperation.NOT_EQUAL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isGreaterThanEqual() {
    return op.equals(SearchOperation.GREATER_THAN_EQUAL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isGreaterThan() {
    return op.equals(SearchOperation.GREATER_THAN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isLessThanEqual() {
    return op.equals(SearchOperation.LESS_THAN_EQUAL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isLessThan() {
    return op.equals(SearchOperation.LESS_THAN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isIn() {
    return op.equals(SearchOperation.IN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNotIn() {
    return op.equals(SearchOperation.NOT_IN);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNull() {
    return op.equals(SearchOperation.IS_NULL);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isNotNull() {
    return op.equals(SearchOperation.IS_NOT_NULL);
  }

  public static Set<SearchCriteria> merge(Set<SearchCriteria> criterias,
      SearchCriteria... criteria) {
    if (criteria == null || criteria.length == 0) {
      return criterias;
    }
    Set<SearchCriteria> merged = new HashSet<>(criterias);
    merged.addAll(List.of(criteria));
    return merged;
  }

  public static SearchCriteria greaterThan(String key, Long value) {
    return new SearchCriteria(key, value, SearchOperation.GREATER_THAN);
  }

  public static SearchCriteria greaterThan(String key, LocalDateTime value) {
    return new SearchCriteria(key, value, SearchOperation.GREATER_THAN);
  }

  public static SearchCriteria lessThan(String key, Long value) {
    return new SearchCriteria(key, value, SearchOperation.LESS_THAN);
  }

  public static SearchCriteria lessThan(String key, LocalDateTime value) {
    return new SearchCriteria(key, value, SearchOperation.LESS_THAN);
  }

  public static SearchCriteria greaterThanEqual(String key, Long value) {
    return new SearchCriteria(key, value, SearchOperation.GREATER_THAN_EQUAL);
  }

  public static SearchCriteria greaterThanEqual(String key, LocalDateTime value) {
    return new SearchCriteria(key, value, SearchOperation.GREATER_THAN_EQUAL);
  }

  public static SearchCriteria lessThanEqual(String key, Long value) {
    return new SearchCriteria(key, value, SearchOperation.LESS_THAN_EQUAL);
  }

  public static SearchCriteria lessThanEqual(String key, LocalDateTime value) {
    return new SearchCriteria(key, value, SearchOperation.LESS_THAN_EQUAL);
  }

  public static SearchCriteria equal(String key, Object value) {
    return new SearchCriteria(key, value, SearchOperation.EQUAL);
  }

  public static SearchCriteria notEqual(String key, Object value) {
    return new SearchCriteria(key, value, SearchOperation.NOT_EQUAL);
  }

  public static SearchCriteria match(String key, String value) {
    return new SearchCriteria(key, value, SearchOperation.MATCH);
  }

  public static SearchCriteria matchEnd(String key, String value) {
    return new SearchCriteria(key, value, SearchOperation.MATCH_END);
  }

  public static SearchCriteria notMatch(String key, String value) {
    return new SearchCriteria(key, value, SearchOperation.NOT_MATCH);
  }

  public static SearchCriteria notMatchEnd(String key, String value) {
    return new SearchCriteria(key, value, SearchOperation.NOT_MATCH_END);
  }

  public static <T> SearchCriteria in(String key, Collection<T> ids) {
    return new SearchCriteria(key, ids, SearchOperation.IN);
  }

  public static <T> SearchCriteria notIn(String key, Collection<T> ids) {
    return new SearchCriteria(key, ids, SearchOperation.NOT_IN);
  }

  public static <T> SearchCriteria in(String key, T[] ids) {
    return new SearchCriteria(key, ids, SearchOperation.IN);
  }

  public static <T> SearchCriteria notIn(String key, T[] ids) {
    return new SearchCriteria(key, ids, SearchOperation.NOT_IN);
  }

  public static <T> SearchCriteria isNull(String key) {
    return new SearchCriteria(key, null, SearchOperation.IS_NULL);
  }

  public static <T> SearchCriteria isNotNull(String key) {
    return new SearchCriteria(key, null, SearchOperation.IS_NOT_NULL);
  }

  public static Set<SearchCriteria> criteria(SearchCriteria... criteria) {
    if (nonNull(criteria)) {
      return new HashSet<>(Arrays.asList(criteria));
    }
    return null;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String toConditionString(String alias) {
    if (!isValidCriteria()) {
      return "";
    }
    String safeAlias = isNotEmpty(alias) ? alias + "." : "";
    return switch (op) {
      case GREATER_THAN -> safeAlias + key + " > " + value;
      case LESS_THAN -> safeAlias + key + " < " + value;
      case GREATER_THAN_EQUAL -> safeAlias + key + " >= " + value;
      case LESS_THAN_EQUAL -> safeAlias + key + " <= " + value;
      case EQUAL -> safeAlias + key + " = '" + value + "'";
      case NOT_EQUAL -> safeAlias + key + " <> '" + value + "'";
      case MATCH -> safeAlias + key + " like '%" + value + "%'";
      case MATCH_END -> safeAlias + key + " like '" + value + "%'";
      case NOT_MATCH -> safeAlias + key + " not like '%" + value + "%'";
      case NOT_MATCH_END -> safeAlias + key + " not like '" + value + "%'";
      case IN -> safeAlias + key + " in (" + getInValues() + ")";
      case NOT_IN -> safeAlias + key + " not in (" + getInValues() + ")";
      default -> "";
    };
  }

  private String getInValues() {
    StringBuilder values = new StringBuilder();
    if (value instanceof Collection) {
      for (Object v : (Collection<?>) value) {
        values.append(v).append(",");
      }
    } else if (value.getClass().isArray()) {
      for (Object v : (Object[]) value) {
        values.append(v).append(",");
      }
    } else if (value.getClass() == String.class) {
      String[] valuesString = value.toString().split(",");
      for (String v : valuesString) {
        values.append(v).append(",");
      }
    } else {
      values.append(value);
    }
    return StringUtils.removeEnd(values.toString(), ",");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchCriteria that = (SearchCriteria) o;
    return (nonNull(that.key) && key.equalsIgnoreCase(that.key)) &&
        (nonNull(that.op) && op.getValue().equalsIgnoreCase(that.op.getValue()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, op);
  }

  public static final class Builder {

    private String key;
    private Object value;
    private SearchOperation op;

    private Builder() {
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder value(Object value) {
      this.value = value;
      return this;
    }

    public Builder op(SearchOperation op) {
      this.op = op;
      return this;
    }

    public SearchCriteria build() {
      return new SearchCriteria(this);
    }
  }
}
