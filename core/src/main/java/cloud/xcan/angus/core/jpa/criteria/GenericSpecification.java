package cloud.xcan.angus.core.jpa.criteria;

import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_FORMAT_ERROR_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.PARAM_FORMAT_ERROR_T;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_KEY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_T2;
import static cloud.xcan.angus.spec.utils.DateUtils.getLocalDateTime;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.ProtocolAssert;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.jpa.domain.Specification;

/**
 * Only supports numeric and date type range query.
 */
@Getter
@Slf4j
public class GenericSpecification<T> implements Specification<T> {

  private final Set<SearchCriteria> criteria;

  public GenericSpecification() {
    this(null);
  }

  public GenericSpecification(Set<SearchCriteria> filters) {
    if (nonNull(filters)) {
      this.criteria = new HashSet<>(filters);
    } else {
      this.criteria = new HashSet<>();
    }
  }

  public Specification<?> toPredicate(SearchCriteria... criteria) {
    return new GenericSpecification<>(SearchCriteria.criteria(criteria));
  }

  public void add(SearchCriteria criteria0) {
    criteria.add(criteria0);
  }

  @Override
  public Predicate toPredicate(@NonNullable Root<T> root, @NonNullable CriteriaQuery<?> query,
      CriteriaBuilder cb) {
    Predicate predicate = cb.conjunction();
    for (SearchCriteria criteria0 : criteria) {
      if (criteria0.isIgnoreFields() || criteria0.isNotValidCriteria()) {
        continue;
      }
      if (criteria0.getOp().noValue()) {
        if (criteria0.getOp().equals(SearchOperation.IS_NULL)) {
          predicate.getExpressions().add(cb.isNull(root.get(criteria0.getKey())));
        } else if (criteria0.getOp().equals(SearchOperation.IS_NOT_NULL)) {
          predicate.getExpressions().add(cb.isNotNull(root.get(criteria0.getKey())));
        }
        continue;
      }
      Object opValue = criteria0.getValue();
      Class<?> keyType = root.get(criteria0.getKey()).getJavaType();
      if (isDateTypeKey(keyType)) {
        LocalDateTime dateValue;
        Object value = criteria0.getValue();
        if (value instanceof LocalDateTime) {
          dateValue = (LocalDateTime) value;
        } else {
          dateValue = getLocalDateTime(value.toString().replaceAll("\"", ""));
        }
        ProtocolAssert.assertNotNull(dateValue, PARAM_FORMAT_ERROR_T, PARAM_FORMAT_ERROR_KEY,
            new Object[]{criteria0.getKey(), value});
        if (criteria0.getOp().equals(SearchOperation.GREATER_THAN)) {
          predicate.getExpressions().add(cb.greaterThan(root.<LocalDateTime>get(criteria0.getKey()),
              cb.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN)) {
          predicate.getExpressions()
              .add(cb.lessThan(root.<LocalDateTime>get(criteria0.getKey()), cb.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.GREATER_THAN_EQUAL)) {
          predicate.getExpressions()
              .add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get(criteria0.getKey()),
                  cb.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN_EQUAL)) {
          predicate.getExpressions()
              .add(cb.lessThanOrEqualTo(root.<LocalDateTime>get(criteria0.getKey()),
                  cb.literal(dateValue)));
        } else if (criteria0.getOp().equals(SearchOperation.EQUAL)) {
          predicate.getExpressions()
              .add(cb.equal(root.<LocalDateTime>get(criteria0.getKey()), cb.literal(dateValue)));
        }
      } else {
        String stringValue = safeStringValue(opValue.toString());
        if (criteria0.getOp().equals(SearchOperation.GREATER_THAN)) {
          predicate.getExpressions().add(cb.greaterThan(
              root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN)) {
          predicate.getExpressions().add(cb.lessThan(
              root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.GREATER_THAN_EQUAL)) {
          predicate.getExpressions().add(cb.greaterThanOrEqualTo(
              root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.LESS_THAN_EQUAL)) {
          predicate.getExpressions().add(cb.lessThanOrEqualTo(
              root.get(criteria0.getKey()), Long.parseLong(stringValue)));
        } else if (criteria0.getOp().equals(SearchOperation.EQUAL)) {
          if (keyType.isEnum()) {
            Value<?>[] values = (Value<?>[]) keyType.getEnumConstants();
            for (Value<?> value : values) {
              if (stringValue.equalsIgnoreCase((String) value.getValue())) {
                predicate.getExpressions().add(cb.equal(root.get(criteria0.getKey()), value));
              }
            }
          } else {
            if (nonNull(toBooleanObject(opValue.toString()))) {
              predicate.getExpressions().add(cb.equal(root.get(criteria0.getKey()),
                  BooleanUtils.toBoolean(opValue.toString())));
            } else if (opValue instanceof String) {
              predicate.getExpressions().add(cb.equal(root.get(criteria0.getKey()), stringValue));
            } else {
              predicate.getExpressions().add(cb.equal(root.get(criteria0.getKey())
                  .as(opValue.getClass()), opValue));
            }
          }
        } else if (criteria0.getOp().equals(SearchOperation.NOT_EQUAL)) {
          if (keyType.isEnum()) {
            Value<?>[] values = (Value<?>[]) keyType.getEnumConstants();
            for (Value<?> value : values) {
              if (stringValue.equalsIgnoreCase((String) value.getValue())) {
                predicate.getExpressions().add(cb.notEqual(root.get(criteria0.getKey()), value));
              }
            }
          } else {
            if (nonNull(toBooleanObject(opValue.toString()))) {
              predicate.getExpressions().add(cb.notEqual(root.get(criteria0.getKey()),
                  BooleanUtils.toBoolean(opValue.toString())));
            } else if (opValue instanceof String) {
              predicate.getExpressions().add(cb.notEqual(root.get(criteria0.getKey()), stringValue));
            } else {
              predicate.getExpressions().add(cb.notEqual(root.get(criteria0.getKey())
                  .as(opValue.getClass()), opValue));
            }
          }
        } else if (criteria0.getOp().equals(SearchOperation.IN)) {
          In<?> inClause = getInCriteria(root, cb, criteria0, opValue, safeInValue(stringValue));
          if (Objects.isNull(inClause)) {
            continue;
          }
          predicate.getExpressions().add(cb.and(inClause));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_IN)) {
          In<?> inClause = getInCriteria(root, cb, criteria0, opValue, safeInValue(stringValue));
          if (Objects.isNull(inClause)) {
            continue;
          }
          predicate.getExpressions().add(cb.and(cb.not(inClause)));
        } else if (criteria0.getOp().equals(SearchOperation.MATCH)) {
          predicate.getExpressions()
              .add(cb.like(root.get(criteria0.getKey()), "%" + stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.MATCH_END)) {
          predicate.getExpressions().add(cb.like(root.get(criteria0.getKey()), stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_MATCH)) {
          predicate.getExpressions()
              .add(cb.notLike(root.get(criteria0.getKey()), "%" + stringValue + "%"));
        } else if (criteria0.getOp().equals(SearchOperation.NOT_MATCH_END)) {
          predicate.getExpressions()
              .add(cb.notLike(root.get(criteria0.getKey()), stringValue + "%"));
        }
      }
    }
    return predicate;
  }

  private boolean isDateTypeKey(Class<?> keyClass) {
    return keyClass == LocalDateTime.class || keyClass == Date.class
        || keyClass == LocalDate.class || keyClass == LocalTime.class;
  }

  private In<?> getInCriteria(Root<T> root, CriteriaBuilder cb, SearchCriteria criteria,
      Object opValue, String stringValue) {
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
    ProtocolAssert.throw0(UNSUPPORTED_FILTER_FIELD_T2, UNSUPPORTED_FILTER_FIELD_KEY,
        new Object[]{criteria.getKey(), criteria.getOp().getValue()});
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
