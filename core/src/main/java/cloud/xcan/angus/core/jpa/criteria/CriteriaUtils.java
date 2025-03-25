package cloud.xcan.angus.core.jpa.criteria;

import static cloud.xcan.angus.remote.search.SearchCriteria.INFO_SCOPE_KEY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.angus.remote.InfoScope;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.DateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class CriteriaUtils {

  public static boolean containsKey(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return false;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsAndRemove(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return false;
    }
    boolean contains = false;
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        contains = true;
        removedCriteria.add(criteria);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return contains;
  }

  public static boolean contains(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || Objects.isNull(op)) {
      return false;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsAndRemove(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return false;
    }
    boolean contains = false;
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        contains = true;
        removedCriteria.add(criteria);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return contains;
  }

  public static List<SearchCriteria> find(Collection<SearchCriteria> filters, String key) {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters) || isEmpty(key)) {
      return criteria;
    }
    for (SearchCriteria filter : filters) {
      if (key.equals(filter.getKey())) {
        criteria.add(filter);
      }
    }
    return criteria;
  }

  public static List<SearchCriteria> findAndRemove(Collection<SearchCriteria> filters, String key) {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters) || isEmpty(key)) {
      return criteria;
    }
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria filter : filters) {
      if (key.equals(filter.getKey())) {
        criteria.add(filter);
        removedCriteria.add(filter);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return criteria;
  }

  public static List<SearchCriteria> find(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters) || isEmpty(key) || Objects.isNull(op)) {
      return criteria;
    }
    for (SearchCriteria filter : filters) {
      if (key.equals(filter.getKey()) && op.equals(filter.getOp())) {
        criteria.add(filter);
      }
    }
    return criteria;
  }

  public static List<SearchCriteria> findAndRemove(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    List<SearchCriteria> criteria = new ArrayList<>();
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return criteria;
    }
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria filter : filters) {
      if (key.equals(filter.getKey()) && op.equals(filter.getOp())) {
        criteria.add(filter);
        removedCriteria.add(filter);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return criteria;
  }

  public static SearchCriteria findFirst(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        return criteria;
      }
    }
    return null;
  }

  public static SearchCriteria findFirstAndRemove(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    SearchCriteria first = null;
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        if (isNull(first)) {
          first = criteria;
        }
        removedCriteria.add(criteria);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return first;
  }

  public static SearchCriteria findFirst(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return null;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        return criteria;
      }
    }
    return null;
  }

  public static SearchCriteria findFirstAndRemove(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return null;
    }
    SearchCriteria first = null;
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        if (isNull(first)) {
          first = criteria;
        }
        removedCriteria.add(criteria);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return first;
  }

  public static List<String> findValue(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    List<SearchCriteria> criteria = find(filters, key);
    return isEmpty(criteria) ? null : criteria.stream()
        .map(c -> safeStringValue(c.getValue().toString())).collect(Collectors.toList());
  }

  public static List<String> findValueAndRemove(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    List<SearchCriteria> criteria = findAndRemove(filters, key);
    return isEmpty(criteria) ? null : criteria.stream()
        .map(c -> safeStringValue(c.getValue().toString())).collect(Collectors.toList());
  }

  public static List<String> findValue(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    List<SearchCriteria> criteria = find(filters, key, op);
    if (isEmpty(criteria)) {
      return null;
    }
    return criteria.stream().map(c -> safeStringValue(c.getValue().toString()))
        .collect(Collectors.toList());
  }

  public static List<String> findValueAndRemove(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    List<SearchCriteria> criteria = findAndRemove(filters, key, op);
    if (isEmpty(criteria)) {
      return null;
    }
    return criteria.stream().map(c -> safeStringValue(c.getValue().toString()))
        .collect(Collectors.toList());
  }

  public static String findFirstValue(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey())) {
        return safeStringValue(criteria.getValue().toString());
      }
    }
    return null;
  }

  public static String findFirstValueAndRemove(Collection<SearchCriteria> filters, String key) {
    if (isEmpty(filters) || isEmpty(key)) {
      return null;
    }
    String first = null;
    List<SearchCriteria> searchCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && criteria.isValidCriteria()) {
        if (isNull(first)) {
          first = safeStringValue(criteria.getValue().toString());
        }
        searchCriteria.add(criteria);
      }
    }
    if (isNotEmpty(searchCriteria)) {
      filters.removeAll(searchCriteria);
    }
    return first;
  }

  public static String findFirstValue(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return null;
    }
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        return safeStringValue(criteria.getValue().toString());
      }
    }
    return null;
  }

  public static String findFirstValueAndRemove(Collection<SearchCriteria> filters, String key,
      SearchOperation op) {
    if (isEmpty(filters) || isEmpty(key) || isNull(op)) {
      return null;
    }
    String first = null;
    List<SearchCriteria> removedCriteria = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      if (key.equals(criteria.getKey()) && op.equals(criteria.getOp())) {
        if (isNull(first)) {
          first = criteria.getValue().toString().replaceAll("\"", "");
        }
        removedCriteria.add(criteria);
      }
    }
    if (isNotEmpty(removedCriteria)) {
      filters.removeAll(removedCriteria);
    }
    return first;
  }

  public static Set<String> getFilterInValue(Collection<SearchCriteria> filters, String key) {
    Set<String> values = new LinkedHashSet<>();
    if (isNotEmpty(filters) && isNotEmpty(key)) {
      List<SearchCriteria> criteria = find(filters, key, SearchOperation.IN);
      if (isNotEmpty(filters)) {
        for (SearchCriteria criteria0 : criteria) {
          if (nonNull(criteria0.getValue())) {
            parseInValues(values, criteria0);
          }
        }
      }
    }
    return values;
  }

  public static Set<String> getFilterInValueAndRemove(Collection<SearchCriteria> filters,
      String key) {
    Set<String> values = new LinkedHashSet<>();
    if (isNotEmpty(filters) && isNotEmpty(key)) {
      List<SearchCriteria> criteria = findAndRemove(filters, key, SearchOperation.IN);
      for (SearchCriteria criteria0 : criteria) {
        if (nonNull(criteria0.getValue())) {
          parseInValues(values, criteria0);
        }
      }
    }
    return values;
  }

  public static String getFilterInFirstValue(Collection<SearchCriteria> filters, String key) {
    if (isNotEmpty(filters)) {
      List<SearchCriteria> criteria = find(filters, key, SearchOperation.IN);
      if (isNotEmpty(criteria)) {
        if (criteria.get(0) instanceof Collection) {
          return StringUtils.join(((Collection<?>) criteria.get(0)).toArray(), ",");
        } else if (criteria.get(0).getClass().isArray()) {
          return StringUtils.join(criteria.get(0), ",");
        }
        String sv = criteria.get(0).getValue().toString().replaceAll("\"", "");
        sv = StringUtils.removeStart(sv, "[");
        sv = StringUtils.removeEnd(sv, "]");
        return sv;
      }
    }
    return null;
  }

  public static Set<String> findAllIdInAndEqualValues(Set<SearchCriteria> filters,
      String key, boolean removeCriteria) {
    Set<String> values = new LinkedHashSet<>();
    List<String> equalValues = removeCriteria
        ? findValueAndRemove(filters, key, SearchOperation.EQUAL)
        : findValue(filters, key, SearchOperation.EQUAL);
    if (isNotEmpty(equalValues)) {
      values.addAll(equalValues);
    }
    Set<String> inValues = removeCriteria ? getFilterInValueAndRemove(filters, key)
        : getFilterInValue(filters, key);
    if (isNotEmpty(inValues)) {
      values.addAll(inValues);
    }
    return values;
  }

  public static boolean findAdminFlagInCriteria(Set<SearchCriteria> filters,
      boolean removeCriteria) {
    SearchCriteria criteria = removeCriteria ? findFirstAndRemove(filters, "adminFlag")
        : findFirst(filters, "adminFlag");
    if (nonNull(criteria) && criteria.isValidCriteria()) {
      return Boolean.parseBoolean(safeStringValue(criteria.getValue().toString()));
    }
    return false;
  }

  public static InfoScope findInfoScope(Collection<SearchCriteria> filters) {
    String infoScope = findFirstValueAndRemove(filters, INFO_SCOPE_KEY);
    if (isEmpty(infoScope)) {
      return InfoScope.DETAIL;
    }
    return InfoScope.valueOf(infoScope);
  }

  public static InfoScope findInfoScope(Collection<SearchCriteria> filters,
      InfoScope defaultInfoScope) {
    String infoScope = findFirstValueAndRemove(filters, INFO_SCOPE_KEY);
    if (isEmpty(infoScope)) {
      return defaultInfoScope;
    }
    return InfoScope.valueOf(infoScope);
  }

  public static List<String> getFilterMatchValue(Collection<SearchCriteria> filters, String key) {
    List<String> values = new ArrayList<>();
    if (isNotEmpty(filters) && isNotEmpty(key)) {
      List<SearchCriteria> criteria = find(filters, key);
      if (isNotEmpty(filters)) {
        for (SearchCriteria criteria0 : criteria) {
          if (nonNull(criteria0.getValue()) && (criteria0.isMatchSearch())) {
            values.add(safeStringValue(criteria0.getValue().toString()));
          }
        }
      }
    }
    return values;
  }

  public static String getFilterMatchFirstValue(Collection<SearchCriteria> filters, String key) {
    if (isNotEmpty(filters)) {
      List<SearchCriteria> criteria = find(filters, key);
      if (isNotEmpty(criteria)) {
        for (SearchCriteria criteria0 : criteria) {
          if (nonNull(criteria0.getValue()) && (criteria0.isMatchSearch())) {
            return safeStringValue(criteria0.getValue().toString());
          }
        }
      }
    }
    return null;
  }

  public static String getFilterMatchFirstValueAndRemove(Collection<SearchCriteria> filters,
      String key) {
    String value = null;
    if (isNotEmpty(filters)) {
      List<SearchCriteria> criteria = find(filters, key);
      if (isNotEmpty(criteria)) {
        for (SearchCriteria criteria0 : criteria) {
          if (nonNull(criteria0.getValue()) && (criteria0.isMatchSearch())) {
            if (isEmpty(value)) {
              value = safeStringValue(criteria0.getValue().toString());
            }
            filters.remove(criteria0);
          }
        }
      }
    }
    return value;
  }

  public static String findMatchAndEqualValue(Set<SearchCriteria> filters,
      String key, boolean removeCriteria) {
    // Equal has higher priority than match
    String equalValue = removeCriteria
        ? findFirstValueAndRemove(filters, key, SearchOperation.EQUAL)
        : findFirstValue(filters, key, SearchOperation.EQUAL);
    if (isNotEmpty(equalValue)) {
      return equalValue;
    }
    String matchValue = removeCriteria
        ? findFirstValueAndRemove(filters, key, SearchOperation.MATCH)
        : findFirstValue(filters, key, SearchOperation.MATCH);
    if (isNotEmpty(matchValue)) {
      return matchValue;
    }
    return removeCriteria ? findFirstValueAndRemove(filters, key, SearchOperation.MATCH_END)
        : findFirstValue(filters, key, SearchOperation.MATCH_END);
  }

  private static void parseInValues(Set<String> values, SearchCriteria criteria) {
    if (criteria.getValue() instanceof Collection<?> values_) {
      for (Object value : values_) {
        values.add(value.toString());
      }
    } else if (criteria.getValue().getClass().isArray()) {
      Object[] values_ = (Object[]) criteria.getValue();
      for (Object value : values_) {
        values.add(value.toString());
      }
    } else {
      String sv = safeStringInValue(criteria.getValue().toString());
      values.addAll(Arrays.asList(sv.split(",")));
    }
  }

  public static String assembleGrantPermissionCondition(Set<SearchCriteria> criteria, String alias,
      String permission) {
    String grantSql = "";
    String grantValue = findFirstValue(criteria, "hasPermission");
    if (isNotEmpty(grantValue)) {
      grantValue = grantValue.replaceAll("\"", "");
      if (grantValue.equalsIgnoreCase(permission)) {
        // Fix "%GRANT%" : Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
        grantSql = " AND " + alias + ".auths LIKE CONCAT('%','" + permission + "','%')";
      }
    }
    return grantSql;
  }

  public static String assembleHasPermissionCondition(Set<SearchCriteria> criteria, String alias) {
    String permissionSql = "";
    String permissionValue = findFirstValue(criteria, "hasPermission");
    if (isNotEmpty(permissionValue)) {
      permissionValue = permissionValue.replaceAll("\"", "");
      // Fix "%GRANT%" : Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
      permissionSql = " AND " + alias + ".auths LIKE CONCAT('%','" + permissionValue + "','%')";
    }
    return permissionSql;
  }

  public static String assembleSearchNameCondition(String searchValue, String alias) {
    if (isEmpty(searchValue)) {
      return "";
    }
    return " AND MATCH (" + alias + ".`name`) AGAINST ('" + searchValue + "' IN BOOLEAN MODE) ";
  }

  public static String getInConditionValue(Collection<Long> ids) {
    return "(" + StringUtils.join(ids, ",") + ")";
  }

  public static String getInConditionValue(String authObjectIds) {
    return "(" + authObjectIds + ")";
  }

  public static String getNameFilterValue(Set<SearchCriteria> criteria) {
    String searchValue = findFirstValue(criteria, "name");
    return isEmpty(searchValue) ? "" : searchValue;
  }

  public static void timestampStringToLong(Set<SearchCriteria> criteria) {
    String startTimeValue = findFirstValueAndRemove(criteria, "timestamp",
        SearchOperation.GREATER_THAN_EQUAL);
    if (isNotEmpty(startTimeValue)) {
      if (!NumberUtils.isDigits(startTimeValue)) {
        Date startDate = DateUtils.parseByDateTimePattern(startTimeValue);
        criteria.add(SearchCriteria.greaterThanEqual("timestamp", startDate.getTime()));
      } else {
        criteria.add(SearchCriteria.greaterThanEqual("timestamp", Long.parseLong(startTimeValue)));
      }
    }
    String endTimeValue = findFirstValueAndRemove(criteria, "timestamp",
        SearchOperation.LESS_THAN_EQUAL);
    if (isNotEmpty(endTimeValue)) {
      if (!NumberUtils.isDigits(endTimeValue)) {
        Date endDate = DateUtils.parseByDateTimePattern(endTimeValue);
        criteria.add(SearchCriteria.lessThanEqual("timestamp", endDate.getTime()));
      } else {
        criteria.add(SearchCriteria.lessThanEqual("timestamp", Long.parseLong(endTimeValue)));
      }
    }
  }
}
