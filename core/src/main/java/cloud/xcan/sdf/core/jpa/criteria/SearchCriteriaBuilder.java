package cloud.xcan.sdf.core.jpa.criteria;

import static cloud.xcan.sdf.api.ApiConstant.DEFAULT_ORDER_BY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_T;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_MATCH_FILTER_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_MATCH_FILTER_T;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_NOT_FILTER_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_NOT_FILTER_T;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_ORDER_BY_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_ORDER_BY_T;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_RANGE_FILTER_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_RANGE_FILTER_T;
import static cloud.xcan.sdf.api.search.SearchOperation.isInOrNotSearch;
import static cloud.xcan.sdf.api.search.SearchOperation.isMatchSearch;
import static cloud.xcan.sdf.api.search.SearchOperation.isRangeSearch;

import cloud.xcan.sdf.api.AbstractQuery;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.utils.BeanFieldUtils;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.CollectionUtils;

public class SearchCriteriaBuilder<T extends AbstractQuery> {

  public static final List<String> EXCLUDED_FILTER_FIELDS = List.of("pageNo", "pageSize",
      "orderBy", "orderSort", "filters");

  public static Map<String, Field[]> DTO_FIELDS = new ConcurrentHashMap<>();
  public static Map<String, List<String>> DTO_NAMES = new ConcurrentHashMap<>();
  public static Map<String, Set<String>> DTO_IDS = new ConcurrentHashMap<>();

  public static Map<String, Set<String>> SUB_TABLE_FIELDS = new ConcurrentHashMap<>();
  public static Map<String, Set<String>> RANGE_SEARCH_FIELDS = new ConcurrentHashMap<>();
  // Fix: match index field must be ordered  Map<String, Set<String>> -> Map<String, LinkedHashSet<String>>
  public static Map<String, LinkedHashSet<String>> MATCH_SEARCH_FIELDS = new ConcurrentHashMap<>();
  public static Map<String, Set<String>> ORDER_BY_FIELDS = new ConcurrentHashMap<>();
  public static Map<String, Set<String>> IN_AND_NOT_FIELDS = new ConcurrentHashMap<>();

  private final T dto;
  private final String key;
  private boolean timestampStringToLong = false;

  public SearchCriteriaBuilder(T dto) {
    this.dto = dto;
    this.key = dto.getClass().getName();
    cacheNamesAndFields();
  }

  public static String[] getSubTableFields(Class<?> dtoClass) {
    Set<String> fields = SUB_TABLE_FIELDS.get(dtoClass.getName());
    if (CollectionUtils.isEmpty(fields)) {
      throw new IllegalStateException("SubTable fields are not set");
    }
    return fields.toArray(new String[0]);
  }

  public static String[] getRangeSearchFields(Class<?> dtoClass) {
    Set<String> fields = RANGE_SEARCH_FIELDS.get(dtoClass.getName());
    if (CollectionUtils.isEmpty(fields)) {
      throw new IllegalStateException("Range search fields are not set");
    }
    return fields.toArray(new String[0]);
  }

  public static String[] getMatchSearchFields(Class<?> dtoClass) {
    Set<String> fields = MATCH_SEARCH_FIELDS.get(dtoClass.getName());
    if (CollectionUtils.isEmpty(fields)) {
      throw new IllegalStateException("Match search fields are not set");
    }
    return fields.toArray(new String[0]);
  }

  public static String[] getOrderByFields(Class<?> dtoClass) {
    Set<String> fields = ORDER_BY_FIELDS.get(dtoClass.getName());
    if (CollectionUtils.isEmpty(fields)) {
      throw new IllegalStateException("orderBy fields are not set");
    }
    return fields.toArray(new String[0]);
  }

  private void cacheNamesAndFields() {
    Field[] fdf = DTO_FIELDS.get(key);
    if (Objects.isNull(fdf) || fdf.length <= 0) {
      DTO_FIELDS.put(key, safeFilterFields());
    }
    List<String> fdn = DTO_NAMES.get(key);
    if (CollectionUtils.isEmpty(fdn)) {
      DTO_NAMES.put(key, BeanFieldUtils.getPropertyNames(dto.getClass()));
    }
    Set<String> ids = DTO_IDS.get(key);
    if (CollectionUtils.isEmpty(ids)) {
      DTO_IDS.put(key, BeanFieldUtils.getIdAnnotationPropertyNames(dto.getClass()));
    }
  }

  public SearchCriteriaBuilder<T> subTableFields(String... subTableFields) {
    if (Objects.isNull(subTableFields)) {
      return this;
    }
    Set<String> cstf = SUB_TABLE_FIELDS.get(key);
    if (CollectionUtils.isEmpty(cstf)) {
      SUB_TABLE_FIELDS.put(key, new HashSet<>(Arrays.asList(subTableFields)));
    }
    return this;
  }

  public SearchCriteriaBuilder<T> inAndNotFields(String... inAndNotFields) {
    if (Objects.isNull(inAndNotFields)) {
      return this;
    }
    Set<String> nots = IN_AND_NOT_FIELDS.get(key);
    if (CollectionUtils.isEmpty(nots)) {
      IN_AND_NOT_FIELDS.put(key, new HashSet<>(Arrays.asList(inAndNotFields)));
    }
    return this;
  }

  public SearchCriteriaBuilder<T> rangeSearchFields(String... rangeSearchFields) {
    if (Objects.isNull(rangeSearchFields)) {
      return this;
    }
    Set<String> crsf = RANGE_SEARCH_FIELDS.get(key);
    if (CollectionUtils.isEmpty(crsf)) {
      RANGE_SEARCH_FIELDS.put(key, new HashSet<>(Arrays.asList(rangeSearchFields)));
    }
    return this;
  }

  public SearchCriteriaBuilder<T> matchSearchFields(String... matchSearchFields) {
    if (Objects.isNull(matchSearchFields)) {
      return this;
    }
    Set<String> crsf = MATCH_SEARCH_FIELDS.get(key);
    if (CollectionUtils.isEmpty(crsf)) {
      MATCH_SEARCH_FIELDS.put(key, new LinkedHashSet<>(Arrays.asList(matchSearchFields)));
    }
    return this;
  }

  public SearchCriteriaBuilder<T> orderByFields(String... orderByFields) {
    if (Objects.isNull(orderByFields)) {
      return this;
    }
    Set<String> obf = ORDER_BY_FIELDS.get(key);
    if (CollectionUtils.isEmpty(obf)) {
      ORDER_BY_FIELDS.put(key, new HashSet<>(Arrays.asList(orderByFields)));
    }
    return this;
  }

  public SearchCriteriaBuilder<T> timestampStringToLong(boolean timestampStringToLong) {
    this.timestampStringToLong = timestampStringToLong;
    return this;
  }

  public Set<SearchCriteria> build() {
    // 1、De-duplication filters parameter
    Set<SearchCriteria> filters = new CopyOnWriteArraySet<>(dto.getFilters());

    // 2、Merge filters parameter, The findDto parameter will override filters parameter
    String key = dto.getClass().getName();
    if (ObjectUtils.isNotEmpty(DTO_FIELDS.get(key))) {
      filters.addAll(BeanFieldUtils.getDtoSearchCriteria(DTO_FIELDS.get(key), dto));
    }

    // 3、Delete and check non-contracted query parameters
    List<String> dtoNames = DTO_NAMES.get(key);
    Set<String> rangeSearchFields = RANGE_SEARCH_FIELDS.get(key);
    Set<String> matchSearchFields = MATCH_SEARCH_FIELDS.get(key);
    Set<String> subTableFields = SUB_TABLE_FIELDS.get(key);
    Set<String> inAndNotFields = IN_AND_NOT_FIELDS.get(key);
    Set<String> orderByFields = ORDER_BY_FIELDS.get(key);
    Set<String> idsFields = DTO_IDS.get(key);

    List<SearchCriteria> removedCriterias = new ArrayList<>();
    for (SearchCriteria criteria : filters) {
      // Delete empty parameters
      if (criteria.isNotValidCriteria()) {
        removedCriterias.add(criteria);
        continue;
      }
      // Delete sub-table parameters
      if (ObjectUtils.isNotEmpty(subTableFields) && subTableFields.contains(criteria.getKey())) {
        removedCriterias.add(criteria);
        continue;
      }
      // Verify non-contracted query parameters
      if (CollectionUtils.isEmpty(dtoNames) || !dtoNames.contains(criteria.getKey())) {
        throw CommProtocolException.of(UNSUPPORTED_FILTER_FIELD_T, UNSUPPORTED_FILTER_FIELD_KEY,
            new Object[]{criteria.getKey()});
      }
      // Verify that the type and range operation, Eg: Only indexed id or createdDate fields support range query
      if (isRangeSearch(criteria)) {
        if (ObjectUtils.isNotEmpty(rangeSearchFields) && !rangeSearchFields
            .contains(criteria.getKey())) {
          throw CommProtocolException.of(UNSUPPORTED_RANGE_FILTER_T, UNSUPPORTED_RANGE_FILTER_KEY,
              new Object[]{criteria.getKey()});
        }
      }
      // Verify that the type and match operation, Eg: Only string type and indexed fields support match query
      if (isMatchSearch(criteria)) {
        if (ObjectUtils.isNotEmpty(matchSearchFields) && !matchSearchFields
            .contains(criteria.getKey())) {
          throw CommProtocolException.of(UNSUPPORTED_MATCH_FILTER_T, UNSUPPORTED_MATCH_FILTER_KEY,
              new Object[]{criteria.getKey()});
        }
      }
      // Only allow ID/Identity Annotation field to execute ID, NOT_IN, NOT_EQUAL condition filtering
      if (isInOrNotSearch(criteria)) {
        if (ObjectUtils.isNotEmpty(inAndNotFields) && !idsFields.contains(criteria.getKey())
            && !inAndNotFields.contains(criteria.getKey())) {
          throw CommProtocolException
              .of(UNSUPPORTED_NOT_FILTER_T, UNSUPPORTED_NOT_FILTER_KEY,
                  new Object[]{criteria.getKey()});
        }
      }
      // Check orderBy field
      if (StringUtils.isNotBlank(dto.getOrderBy())) {
        if ((!DEFAULT_ORDER_BY.equalsIgnoreCase(dto.getOrderBy()) &&
            ObjectUtils.isNotEmpty(orderByFields) && !orderByFields.contains(dto.getOrderBy()))) {
          throw CommProtocolException.of(UNSUPPORTED_ORDER_BY_T, UNSUPPORTED_ORDER_BY_KEY,
              new Object[]{dto.getOrderBy()});
        }
      }
    }
    if (ObjectUtils.isNotEmpty(removedCriterias)) {
      filters.removeAll(removedCriterias);
    }

    if (timestampStringToLong) {
      CriteriaUtils.timestampStringToLong(filters);
    }
    return filters;
  }

  public Field[] safeFilterFields() {
    Field[] allFields = FieldUtils.getAllFields(dto.getClass());
    List<Field> safeFields = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(allFields)) {
      for (Field field : allFields) {
        if (!EXCLUDED_FILTER_FIELDS.contains(field.getName())) {
          safeFields.add(field);
        }
      }
    }
    return safeFields.toArray(new Field[0]);
  }
}
