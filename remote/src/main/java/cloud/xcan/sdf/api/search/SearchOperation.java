package cloud.xcan.sdf.api.search;


import static java.util.Objects.nonNull;

import cloud.xcan.sdf.spec.experimental.Value;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public enum SearchOperation implements Value<String> {

  GREATER_THAN,
  LESS_THAN,
  GREATER_THAN_EQUAL,
  LESS_THAN_EQUAL,
  EQUAL,
  NOT_EQUAL,
  MATCH,
  MATCH_END,
  NOT_MATCH,
  NOT_MATCH_END,
  IN,
  NOT_IN,
  IS_NULL,
  IS_NOT_NULL;

  public boolean noValue() {
    return this.equals(IS_NULL) || this.equals(IS_NOT_NULL);
  }

  public static boolean noValue(SearchOperation op) {
    return op.equals(IS_NULL) || op.equals(IS_NOT_NULL);
  }

  public static boolean isValidCriteria(SearchCriteria criteria) {
    if (Objects.isNull(criteria.getOp())) {
      return false;
    }
    if (noValue(criteria.getOp())) {
      return StringUtils.isNotEmpty(criteria.getKey());
    } else {
      return StringUtils.isNotEmpty(criteria.getKey()) && nonNull(criteria.getOp())
          && nonNull(criteria.getValue());
    }
  }

  public static boolean isRangeSearch(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.GREATER_THAN) ||
        criteria.getOp().equals(SearchOperation.LESS_THAN) ||
        criteria.getOp().equals(SearchOperation.GREATER_THAN_EQUAL) ||
        criteria.getOp().equals(SearchOperation.LESS_THAN_EQUAL);
  }

  public static boolean isMatchSearch(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.MATCH) ||
        criteria.getOp().equals(SearchOperation.MATCH_END);
  }

  public static boolean isNotMatchSearch(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.NOT_MATCH)
        || criteria.getOp().equals(SearchOperation.NOT_MATCH_END);
  }

  public static boolean isInSearch(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.IN)
        || criteria.getOp().equals(SearchOperation.NOT_IN);
  }

  public static boolean isInOrNotSearch(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.NOT_EQUAL)
        || criteria.getOp().equals(SearchOperation.IN)
        || criteria.getOp().equals(SearchOperation.NOT_IN);
  }

  public static boolean isEqual(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.EQUAL);
  }

  public static boolean isNotEqual(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.NOT_EQUAL);
  }

  public static boolean isNull(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.IS_NULL);
  }

  public static boolean isNotNull(SearchCriteria criteria) {
    return criteria.getOp().equals(SearchOperation.IS_NOT_NULL);
  }


  @Override
  public String getValue() {
    return this.name();
  }

}
