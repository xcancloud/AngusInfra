package cloud.xcan.angus.core.jpa.criteria;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_FORMAT_ERROR_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_FORMAT_ERROR_T;
import static cloud.xcan.angus.spec.utils.DateUtils.getLocalDateTime;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.experimental.Value;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

/**
 * Only supports numeric and date type range query.
 */
@Getter
@Slf4j
public class GenericSpecification<T> implements Specification<T> {

  private final Set<SearchCriteria> criteria;

  @Setter
  private boolean distinct = false;

  public GenericSpecification() {
    this(null, false);
  }

  public GenericSpecification(Set<SearchCriteria> filters) {
    this(filters, false);
  }

  public GenericSpecification(Set<SearchCriteria> filters, boolean distinct) {
    this.criteria = nonNull(filters) ? new HashSet<>(filters) : new HashSet<>();
    this.distinct = distinct;
  }

  public Specification<?> toPredicate(SearchCriteria... criteria) {
    return new GenericSpecification<>(SearchCriteria.criteria(criteria));
  }

  public void add(SearchCriteria criteria0) {
    criteria.add(criteria0);
  }

  @Override
  public Predicate toPredicate(@NonNullable Root<T> root, @NonNullable CriteriaQuery<?> query,
      CriteriaBuilder builder) {
    // @formatter:off
    query.distinct(distinct);
    List<Predicate> predicates = new ArrayList<>();
    for (SearchCriteria criteria0 : criteria) {
      if (criteria0.isIgnoreFields() || criteria0.isNotValidCriteria()) {
        continue;
      }
      if (criteria0.getOp().noValue()) {
        if (criteria0.getOp().equals(SearchOperation.IS_NULL)) {
          predicates.add(builder.isNull(root.get(criteria0.getKey())));
        } else if (criteria0.getOp().equals(SearchOperation.IS_NOT_NULL)) {
          predicates.add(builder.isNotNull(root.get(criteria0.getKey())));
        }
        continue;
      }
      Object opValue = criteria0.getValue();
      Class<?> keyType = root.get(criteria0.getKey()).getJavaType();
      if (isDateTypeKey(keyType)) {
        Object value = criteria0.getValue();
        LocalDateTime dateValue = value instanceof LocalDateTime ? (LocalDateTime) value
            : getLocalDateTime(value.toString().replaceAll("\"", ""));
        assertNotNull(dateValue, PARAM_FORMAT_ERROR_T, PARAM_FORMAT_ERROR_KEY, new Object[]{criteria0.getKey(), value});
        if (criteria0.getOp().equals(SearchOperation.GREATER_THAN)) {
          predicates.add(builder.greaterThan(root.<LocalDateTime>get(criteria0.getKey()), builder.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN)) {
          predicates.add(builder.lessThan(root.<LocalDateTime>get(criteria0.getKey()), builder.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.GREATER_THAN_EQUAL)) {
          predicates.add(builder.greaterThanOrEqualTo(root.<LocalDateTime>get(criteria0.getKey()), builder.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN_EQUAL)) {
          predicates.add(builder.lessThanOrEqualTo(root.<LocalDateTime>get(criteria0.getKey()), builder.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.EQUAL)) {
          predicates.add(builder.equal(root.<LocalDateTime>get(criteria0.getKey()), builder.literal(dateValue)));
        }
      } else {
        String stringValue = safeStringValue(opValue.toString());
        if (criteria0.getOp().equals(SearchOperation.GREATER_THAN)) {
          predicates.add(builder.greaterThan(root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN)) {
          predicates.add(builder.lessThan(root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.GREATER_THAN_EQUAL)) {
          predicates.add(builder.greaterThanOrEqualTo(root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN_EQUAL)) {
          predicates.add(builder.lessThanOrEqualTo(root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.EQUAL)) {
          if (keyType.isEnum()) {
            Value<?>[] values = (Value<?>[]) keyType.getEnumConstants();
            for (Value<?> value : values) {
              if (stringValue.equalsIgnoreCase((String) value.getValue())) {
                predicates.add(builder.equal(root.get(criteria0.getKey()), value));
              }
            }
          } else {
            if (nonNull(toBooleanObject(opValue.toString()))) {
              predicates.add(builder.equal(root.get(criteria0.getKey()), toBoolean(opValue.toString())));
            } else if (opValue instanceof String) {
              predicates.add(builder.equal(root.get(criteria0.getKey()), stringValue));
            } else {
              predicates.add(builder.equal(root.get(criteria0.getKey()).as(opValue.getClass()), opValue));
            }
          }
        } else if (criteria0.getOp().equals(SearchOperation.NOT_EQUAL)) {
          if (keyType.isEnum()) {
            Value<?>[] values = (Value<?>[]) keyType.getEnumConstants();
            for (Value<?> value : values) {
              if (stringValue.equalsIgnoreCase((String) value.getValue())) {
                predicates.add(builder.notEqual(root.get(criteria0.getKey()), value));
              }
            }
          } else {
            if (nonNull(toBooleanObject(opValue.toString()))) {
              predicates.add(builder.notEqual(root.get(criteria0.getKey()), toBoolean(opValue.toString())));
            } else if (opValue instanceof String) {
              predicates.add(builder.notEqual(root.get(criteria0.getKey()), stringValue));
            } else {
              predicates.add(builder.notEqual(root.get(criteria0.getKey()).as(opValue.getClass()), opValue));
            }
          }
        } else if (criteria0.getOp().equals(SearchOperation.IN)) {
          In<?> inClause = getInCriteria(root, builder, criteria0, opValue, safeInValue(stringValue));
          if (isNull(inClause)) {
            continue;
          }
          predicates.add(builder.and(inClause));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_IN)) {
          In<?> inClause = getInCriteria(root, builder, criteria0, opValue, safeInValue(stringValue));
          if (isNull(inClause)) {
            continue;
          }
          predicates.add(builder.and(builder.not(inClause)));
        } else if (criteria0.getOp().equals(SearchOperation.MATCH)) {
          predicates.add(builder.like(root.get(criteria0.getKey()), "%" + stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.MATCH_END)) {
          predicates.add(builder.like(root.get(criteria0.getKey()), stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_MATCH)) {
          predicates.add(builder.notLike(root.get(criteria0.getKey()), "%" + stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_MATCH_END)) {
          predicates.add(builder.notLike(root.get(criteria0.getKey()), stringValue + "%"));
        }
      }
    }
    return builder.and(predicates.toArray(new Predicate[0]));
    // @formatter:on
  }

  private boolean isDateTypeKey(Class<?> keyClass) {
    return keyClass == LocalDateTime.class || keyClass == Date.class
        || keyClass == LocalDate.class || keyClass == LocalTime.class;
  }

  private In<?> getInCriteria(Root<T> root, CriteriaBuilder cb, SearchCriteria criteria,
      Object opValue, String stringValue) {
    if (isEmpty(opValue) && isEmpty(stringValue)) {
      return null;
    }

    In<Object> inClause = cb.in(root.get(criteria.getKey()));
    if (opValue instanceof Collection) {
      for (Object o : (Collection<?>) opValue) {
        inClause.value(o);
      }
      return inClause;
    }
    if (opValue.getClass().isArray()) {
      if (Long.class == root.get(criteria.getKey()).getJavaType()) {
        for (Object v : (Object[]) opValue) {
          inClause.value(Long.parseLong(v.toString()));
        }
        return inClause;
      }
      if (String.class == root.get(criteria.getKey()).getJavaType()) {
        for (String v : (String[]) opValue) {
          inClause.value(v);
        }
        return inClause;
      }
    }

    String[] values = stringValue.split(",");
    Class<?> keyType = root.get(criteria.getKey()).getJavaType();
    if (Long.class == keyType) {
      for (String v : values) {
        inClause.value(Long.valueOf(v.trim()));
      }
      return inClause;
    }
    if (String.class == keyType) {
      for (String v : values) {
        inClause.value(v);
      }
      return inClause;
    }
    if (keyType.isEnum()) {
      for (String v : values) {
        Value<?>[] vs = (Value<?>[]) keyType.getEnumConstants();
        for (Value<?> value : vs) {
          if (v.equalsIgnoreCase((String) value.getValue())) {
            inClause.value(value);
          }
        }
      }
      return inClause;
    }
    return null;
  }

  private Object toBooleanObject(String value) {
    if ("true".equals(value) || ("TRUE").equals(value)) {
      return Boolean.TRUE;
    } else if ("false".equals(value) || ("FALSE").equals(value)) {
      return Boolean.FALSE;
    }
    return null;
  }
}
