package cloud.xcan.angus.persistence.jpa.repository;

import static cloud.xcan.angus.remote.message.ProtocolException.M.UNSUPPORTED_FILTER_FIELD_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.UNSUPPORTED_FILTER_FIELD_T2;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringInValue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.safeStringValue;
import static cloud.xcan.angus.spec.utils.StringUtils.camelToUnder;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.persistence.jpa.JpaMetadataUtils;
import cloud.xcan.angus.persistence.jpa.multitenancy.TenantNativeQuerySupport;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.experimental.Value;
import cloud.xcan.angus.spec.utils.ReflectionUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RegExUtils;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;

public abstract class AbstractSearchRepository<T> implements CustomBaseRepository<T> {

  @Resource
  @PersistenceContext
  public EntityManager entityManager;

  public EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * Non-main mainClz conditions and joins need to be assembled by themselves
   */
  @Override
  public abstract StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<T> mainClz,
      Object[] params, String... match);

  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "*";
  }

  public String getReturnCountCondition(Set<SearchCriteria> criteria, Object[] params) {
    return "count(*)";
  }

  @Override
  public Page<T> find0(Set<SearchCriteria> criteria, Pageable pageable, Class<T> mainClz,
      Function<? super Object[], T> mapper, Object[] params, String[] match) {
    assertNotNull(pageable, "pageable is required");
    assertNotNull(mainClz, "mainClz is required");
    StringBuilder sql = getSqlTemplate(criteria, mainClz, params, match);
    String countSql = sql.toString();
    List<T> ts = getList(criteria, pageable, mainClz, mapper, params, sql);
    if (ts.size() < pageable.getPageSize()) {
      return new PageImpl<>(ts, pageable, ts.size());
    }
    long count = getCount(criteria, mainClz, params, countSql);
    return new PageImpl<>(ts, pageable, count);
  }

  @SuppressWarnings("unchecked")
  public List<T> getList(Set<SearchCriteria> criteria,
      Pageable pageable, Class<T> mainClz, Function<? super Object[], T> mapper,
      Object[] params, StringBuilder sql) {
    if (pageable.getSort().isSorted()) {
      Order order = pageable.getSort().iterator().next();
      sql.append(" ORDER BY ").append(camelToUnder(order.getProperty())).append(" ")
          .append(order.getDirection());
    }
    if (Objects.nonNull(mapper)) {
      Query queryList = getEntityManager().createNativeQuery(
          sql.toString().replaceFirst("%s", getReturnFieldsCondition(criteria, params)));
      queryList.setFirstResult((int) pageable.getOffset());
      queryList.setMaxResults(pageable.getPageSize());
      if (!CollectionUtils.isEmpty(criteria)) {
        setQueryParameter(queryList, criteria, mainClz);
      }
      TenantNativeQuerySupport.bindTenantParameterIfNeeded(queryList, mainClz);
      List<Object[]> result = (List<Object[]>) queryList.getResultList();
      if (isEmpty(result)) {
        return Collections.emptyList();
      }
      return result.stream().map(mapper).collect(Collectors.toList());
    }
    Query queryList = getEntityManager().createNativeQuery(sql.toString()
        .replaceFirst("%s", getReturnFieldsCondition(criteria, params)), mainClz);
    queryList.setFirstResult((int) pageable.getOffset());
    queryList.setMaxResults(pageable.getPageSize());
    if (!CollectionUtils.isEmpty(criteria)) {
      setQueryParameter(queryList, criteria, mainClz);
    }
    TenantNativeQuerySupport.bindTenantParameterIfNeeded(queryList, mainClz);
    return (List<T>) queryList.getResultList();
  }

  public long getCount(Set<SearchCriteria> criteria,
      Class<?> mainClz, Object[] params, String sql) {
    Query queryCount = getEntityManager().createNativeQuery(
        sql.replaceFirst("%s", getReturnCountCondition(criteria, params)));
    if (!CollectionUtils.isEmpty(criteria)) {
      setQueryParameter(queryCount, criteria, mainClz);
    }
    TenantNativeQuerySupport.bindTenantParameterIfNeeded(queryCount, mainClz);
    Object count = queryCount.getSingleResult();
    if (count instanceof BigInteger) {
      return ((BigInteger) count).longValue();
    }
    return (Long) count;
  }

  @Override
  public StringBuilder getCriteriaAliasCondition(Set<SearchCriteria> criteria, Class<T> mainClz,
      String alias, SearchMode mode, Boolean notDeleted, String... match) {
    StringBuilder sql = new StringBuilder();
    boolean hasSearch = false;
    if (isNotEmpty(criteria)) {
      for (SearchCriteria criteria0 : criteria) {
        if (criteria0.isIgnoreFields() || criteria0.isNotValidCriteria()) {
          continue;
        }

        boolean isKeyword = "keyword".equalsIgnoreCase(criteria0.getKey());
        String columnName = null;
        if (!isKeyword) {
          columnName = JpaMetadataUtils.getColumnName(entityManager, mainClz, criteria0.getKey());
          if (isEmpty(columnName)) {
            continue;
          }
        }

        // Same as legacy setOp(MATCH) for keyword, but without mutating criteria0 (shared Set).
        SearchOperation effective = criteria0.getOp();
        if (isKeyword && !criteria0.isInOrNotSearch()) {
          effective = SearchOperation.MATCH;
        }
        String namingKey = criteria0.getOp().getValue() + criteria0.getKey();
        boolean isMatchLikeOp =
            effective.equals(SearchOperation.MATCH) || effective.equals(SearchOperation.MATCH_END)
                || effective.equals(SearchOperation.NOT_MATCH) || effective.equals(
                SearchOperation.NOT_MATCH_END);

        if (effective.equals(SearchOperation.GREATER_THAN)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" > :").append(namingKey);
        } else if (effective.equals(SearchOperation.LESS_THAN)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" < :").append(namingKey);
        } else if (effective.equals(SearchOperation.GREATER_THAN_EQUAL)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" >= :").append(namingKey);
        } else if (effective.equals(SearchOperation.LESS_THAN_EQUAL)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" <= :").append(namingKey);
        } else if (effective.equals(SearchOperation.EQUAL)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" = :").append(namingKey);
        } else if (effective.equals(SearchOperation.NOT_EQUAL)) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" <> :").append(namingKey);
        } else if (!hasSearch && isMatchLikeOp) {
          if (Objects.isNull(mode)) {
            mode = getSearchMode();
          }
          // TODO:: Support fuzzy query of multiple values
          // SELECT * FROM store_goods xc WHERE MATCH (xc.NAME,xc.CODE,xc.introduction,xc.information,xc.apply_edition_type) AGAINST ('COMMUNITY DATACENTER' IN BOOLEAN MODE);
          // SELECT * FROM store_goods xc WHERE xc.apply_edition_type like "%COMMUNITY%" OR xc.apply_edition_type like "%DATACENTER%"
          if (mode.equals(SearchMode.MATCH)) { // @DoInFuture("根据 Datasource 类型拼接全文检索sql")
            // Fix: Unknown column 'hahah' in 'where clause'
            // MATCH(xc.name) AGAINST (hahah IN BOOLEAN MODE) -> MATCH(xc.name) AGAINST ('hahah' IN BOOLEAN MODE)
            String strValue = detectFulltextSearchValue(criteria0.getValue().toString());
            sql.append(" AND MATCH(")
                .append(getAliasMatchFields(mainClz, alias, criteria0, match))
                .append(") AGAINST (")/*.append(criteria0.isMatchSearch() ? "+" : "-")*/
                // Complete progression matching
                /*.append("\"")*/
                .append(strValue)/*.append("\"")*/ /* Note:: Postgres uses two single quotes */
                .append(" IN BOOLEAN MODE)");
          } else {
            String likeColumn = columnName;
            if (isEmpty(likeColumn) && isNotEmpty(match)) {
              likeColumn = JpaMetadataUtils.getColumnName(entityManager, mainClz, match[0]);
            }
            if (isEmpty(likeColumn)) {
              continue;
            }
            String strValue = safeStringValue(criteria0.getValue().toString());
            if (SearchOperation.MATCH.equals(effective)) {
              sql.append(" AND ").append(alias).append(".")
                  .append(likeColumn)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" like \"%%").append(strValue).append("%%\"");
            } else if (SearchOperation.MATCH_END.equals(effective)) {
              sql.append(" AND ").append(alias).append(".")
                  .append(likeColumn)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" like \"").append(strValue).append("%%\"");
            } else if (SearchOperation.NOT_MATCH.equals(effective)) {
              sql.append(" AND ").append(alias).append(".")
                  .append(likeColumn)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" not like \"%%").append(strValue).append("%%\"");
            } else if (SearchOperation.NOT_MATCH_END.equals(effective)) {
              sql.append(" AND ").append(alias).append(".")
                  .append(likeColumn)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" not like \"").append(strValue).append("%%\"");
            }
          }
          hasSearch = true;
        } else if (criteria0.isIn()) {
          if (isEmpty(columnName)) {
            continue;
          }
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" in :").append(namingKey);
        } else if (criteria0.isNotIn()) {
          if (isEmpty(columnName)) {
            continue;
          }
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" not in :").append(namingKey);
        } else if (criteria0.isNull()) {
          if (isEmpty(columnName)) {
            continue;
          }
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" IS NULL ");
        } else if (criteria0.isNotNull()) {
          if (isEmpty(columnName)) {
            continue;
          }
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" IS NOT NULL ");
        }
      }
    }
    if (notDeleted && hasDeletedField(mainClz)) {
      sql.append(" AND ").append(alias).append(".deleted = 0");
    }
    TenantNativeQuerySupport.appendQualifiedTenantClause(sql, alias, mainClz);
    return sql;
  }

  public void setQueryParameter(Query query, Set<SearchCriteria> criteria, Class<?> mainClz) {
    for (SearchCriteria criteria0 : criteria) {
      if (criteria0.isIgnoreFields() || criteria0.isNotValidCriteria()) {
        continue;
      }
      String columnName = JpaMetadataUtils.getColumnName(entityManager, mainClz,
          criteria0.getKey());
      if (isEmpty(columnName)) {
        continue;
      }
      String namingKey = criteria0.getOp().getValue() + criteria0.getKey();
      if (SearchOperation.isMatchSearch(criteria0)) {
        continue;
      }
      if (SearchOperation.isInSearch(criteria0)) {
        Object value = criteria0.getValue();
        if (value instanceof String) {
          query.setParameter(namingKey, new HashSet<>(
              Arrays.asList(safeStringInValue(value.toString()).split(","))));
        } else if (value.getClass().isArray()) {
          Object[] array = (Object[]) criteria0.getValue();
          query.setParameter(namingKey, List.of(array));
        } else {
          // Such as:
          // value instanceof Collection
          query.setParameter(namingKey, criteria0.getValue());
        }
      } else {
        Field f = ReflectionUtils.getField(mainClz, criteria0.getKey());
        if (f == null) {
          throw new IllegalArgumentException(
              "Unknown filter field for entity " + mainClz.getName() + ": " + criteria0.getKey());
        }
        String strValue = safeStringValue(criteria0.getValue().toString());
        if (criteria0.getValue() instanceof Value) {
          query.setParameter(namingKey, ((Value) criteria0.getValue()).getValue());
        } else if (f.getType().isEnum()) {
          Class<?> enumType = f.getType();
          if (Value.class.isAssignableFrom(enumType)) {
            Value<?>[] values = (Value<?>[]) enumType.getEnumConstants();
            boolean matched = false;
            for (Value<?> ev : values) {
              if (strValue.equalsIgnoreCase((String) ev.getValue())) {
                query.setParameter(namingKey, ev.getValue());
                matched = true;
                break;
              }
            }
            if (!matched) {
              throw new IllegalArgumentException(
                  "Unsupported enum value for field '" + criteria0.getKey() + "': " + strValue);
            }
          } else {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Class<? extends Enum> enumClass = (Class<? extends Enum>) enumType;
            Enum<?> constant;
            try {
              constant = Enum.valueOf(enumClass, strValue);
            } catch (IllegalArgumentException ex) {
              try {
                constant = Enum.valueOf(enumClass, strValue.toUpperCase());
              } catch (IllegalArgumentException ex2) {
                throw new IllegalArgumentException(
                    "Unsupported enum value for field '" + criteria0.getKey() + "': " + strValue,
                    ex2);
              }
            }
            query.setParameter(namingKey, constant);
          }
        } else if (criteria0.getValue() instanceof Boolean) {
          query.setParameter(namingKey, criteria0.getValue());
        } else if (isBooleanValue(strValue)) {
          query.setParameter(namingKey, Boolean.valueOf(strValue));
        } else {
          query.setParameter(namingKey, strValue);
        }
      }
    }
  }

  public String getMatchFields(SingleTableEntityPersister step, SearchCriteria criteria0,
      String... match) {
    ProtocolAssert.assertNotEmpty(match, UNSUPPORTED_FILTER_FIELD_T2, UNSUPPORTED_FILTER_FIELD_KEY,
        new Object[]{criteria0.getKey(), criteria0.getOp().getValue()});
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < match.length; i++) {
      sb.append(step.getPropertyColumnNames(match[i])[0]);
      if (i != match.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  public String getAliasMatchFields(Class<T> mainClz, String alias, SearchCriteria criteria0,
      String... match) {
    // match have the highest priority
    if (isNotEmpty(match)) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < match.length; i++) {
        sb.append(alias).append(".").append(
            JpaMetadataUtils.getColumnName(entityManager, mainClz, match[i]));
        if (i != match.length - 1) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    // Repo overwrite
    String fields = getMatchFields();
    if (isNotEmpty(fields)) {
      return fields;
    }
    return JpaMetadataUtils.getColumnName(entityManager, mainClz, criteria0.getKey());
  }

  public boolean hasDeletedField(Class<T> mainClz) {
    try {
      String name = JpaMetadataUtils.getColumnName(entityManager, mainClz, "deleted");
      return isNotEmpty(name);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean hasTenantField(SingleTableEntityPersister step) {
    try {
      String[] names = step.getPropertyColumnNames("tenantId");
      return nonNull(names) && names.length > 0;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isBooleanValue(String value) {
    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
  }

  /**
   * 在布尔全文搜索模式下，你可以使用以下布尔操作符进行搜索：
   * <pre>
   *  +：表示必须包含该关键词。例如：+word.
   *  -：表示必须不包含该关键词。例如：-word.
   *  > 和 <：用于指定词的权重。例如：word >weight.
   *  *：用于指定通配符搜索。例如：word* 匹配 word, words, wording 等。
   *
   *  在 MySQL 的布尔全文搜索模式下，要精确检索包含空格的字符串，可以使用单引号和双引号将关键词括起来。
   *
   *  注意：使用双引号括起来完整匹配字符串性能能提示10倍以上！！！
   *  </pre>
   */
  public static String detectFulltextSearchValue(String strValue) {
    if (isEmpty(strValue) || strValue.startsWith("\"")) {
      return strValue;
    }
    if (strValue.indexOf("+") > 0 || strValue.indexOf("-") > 0 || strValue.indexOf("*") > 0
        || strValue.indexOf(">") > 0 || strValue.indexOf("<") > 0) {
      return "\"" + strValue + "\"";
    }
    strValue = RegExUtils.removeAll(strValue, "'");
    if (strValue.startsWith("/")) {
      // Search uri
      strValue = strValue.replaceAll("/", " +");
      return "\"" + strValue + "\"";
    }
    return "'\"" + strValue + "\"'";
  }

}
