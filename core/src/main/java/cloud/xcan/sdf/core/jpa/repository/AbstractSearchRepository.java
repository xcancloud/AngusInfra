package cloud.xcan.sdf.core.jpa.repository;

import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.UNSUPPORTED_FILTER_FIELD_T2;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.sdf.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.safeStringInValue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.safeStringValue;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.interceptor.TenantInterceptor;
import cloud.xcan.sdf.spec.experimental.Value;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import cloud.xcan.sdf.spec.utils.ReflectionUtils;
import cloud.xcan.sdf.spec.utils.StringUtils;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RegExUtils;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
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
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public abstract StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... fifs);

  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "*";
  }

  public String getReturnCountCondition(Set<SearchCriteria> criterias, Object[] params) {
    return "count(*)";
  }

  @Override
  public Page<T> find0(Set<SearchCriteria> criterias, Pageable pageable, Class<T> mainClass,
      Function<? super Object[], T> mapper, Object[] params, String[] fifs) {
    assertNotNull(pageable, "pageable is required");
    assertNotNull(mainClass, "mainClass is required");
    MetamodelImpl metaModel = (MetamodelImpl) getEntityManager().getMetamodel();
    Map<String, EntityPersister> entityPm = metaModel.entityPersisters();
    SingleTableEntityPersister step = (SingleTableEntityPersister) entityPm
        .get(mainClass.getName());
    StringBuilder sql = getSqlTemplate(step, criterias, params, fifs);
    String countSql = sql.toString();
    List<T> ts = getList(step, criterias, pageable, mainClass, mapper, params, sql);
    if (ts.size() < pageable.getPageSize()) {
      return new PageImpl<>(ts, pageable, ts.size());
    }
    long count = getCount(step, criterias, mainClass, params, countSql);
    return new PageImpl<>(ts, pageable, count);
  }

  @SuppressWarnings("unchecked")
  public List<T> getList(SingleTableEntityPersister step, Set<SearchCriteria> criterias,
      Pageable pageable, Class<T> mainClass, Function<? super Object[], T> mapper,
      Object[] params, StringBuilder sql) {
    Order order = pageable.getSort().get().findFirst().get();
    sql.append(" ORDER BY ").append(StringUtils.camelToUnder(order.getProperty())).append(" ")
        .append(order.getDirection());
    if (Objects.nonNull(mapper)) {
      Query queryList = getEntityManager().createNativeQuery(
          sql.toString().replaceFirst("%s", getReturnFieldsCondition(criterias, params)));
      queryList.setFirstResult((int) pageable.getOffset());
      queryList.setMaxResults(pageable.getPageSize());
      if (!CollectionUtils.isEmpty(criterias)) {
        setQueryParameter(step, queryList, criterias, mainClass);
      }
      List<Object[]> result = (List<Object[]>) queryList.getResultList();
      if (isEmpty(result)) {
        return Collections.emptyList();
      }
      return result.stream().map(mapper).collect(Collectors.toList());
    }
    Query queryList = getEntityManager().createNativeQuery(sql.toString()
        .replaceFirst("%s", getReturnFieldsCondition(criterias, params)), mainClass);
    queryList.setFirstResult((int) pageable.getOffset());
    queryList.setMaxResults(pageable.getPageSize());
    if (!CollectionUtils.isEmpty(criterias)) {
      setQueryParameter(step, queryList, criterias, mainClass);
    }
    return (List<T>) queryList.getResultList();
  }

  public long getCount(SingleTableEntityPersister step, Set<SearchCriteria> criterias,
      Class<?> mainClass, Object[] params, String sql) {
    Query queryCount = getEntityManager().createNativeQuery(
        sql.replaceFirst("%s", getReturnCountCondition(criterias, params)));
    if (!CollectionUtils.isEmpty(criterias)) {
      setQueryParameter(step, queryCount, criterias, mainClass);
    }
    return ((BigInteger) queryCount.getSingleResult()).longValue();
  }

  @Override
  public StringBuilder getCriteriaAliasCondition(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String alias, SearchMode mode, Boolean notDeleted,
      String... fifs) {
    StringBuilder sql = new StringBuilder();
    boolean hasSearch = false;
    if (!CollectionUtils.isEmpty(criterias)) {
      for (SearchCriteria criteria : criterias) {
        if (criteria.isIgnoreFields() || criteria.isNotValidCriteria()) {
          continue;
        }
        String columnName = getColumnName(step, criteria.getKey());
        if (isEmpty(columnName)) {
          continue;
        }
        String namingKey = criteria.getOp().getValue() + criteria.getKey();
        if (criteria.isGreaterThan()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" > :").append(namingKey);
        } else if (criteria.isLessThan()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" < :").append(namingKey);
        } else if (criteria.isGreaterThanEqual()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" >= :").append(namingKey);
        } else if (criteria.isLessThanEqual()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" <= :").append(namingKey);
        } else if (criteria.isEqual()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" = :").append(namingKey);
        } else if (criteria.isNotEqual()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" <> :").append(namingKey);
        } else if (!hasSearch && (criteria.isMatchSearch() || criteria.isNotMatchSearch())) {
          if (Objects.isNull(mode)) {
            mode = getSearchMode();
          }
          // TODO:: Support fuzzy query of multiple values
          // SELECT * FROM store_goods xc WHERE MATCH (xc.NAME,xc.CODE,xc.introduction,xc.information,xc.apply_edition_type) AGAINST ('COMMUNITY DATACENTER' IN BOOLEAN MODE);
          // SELECT * FROM store_goods xc WHERE xc.apply_edition_type like "%COMMUNITY%" OR xc.apply_edition_type like "%DATACENTER%"
          if (mode.equals(SearchMode.MATCH)) { // @DoInFuture("根据 Datasource 类型拼接全文检索sql")
            // Fix: Unknown column 'hahah' in 'where clause'
            // MATCH(xc.name) AGAINST (hahah IN BOOLEAN MODE) -> MATCH(xc.name) AGAINST ('hahah' IN BOOLEAN MODE)
            String strValue = detectFulltextSearchValue(criteria.getValue().toString());
            sql.append(" AND MATCH(")
                .append(getAliasMatchFields(step, alias, criteria, fifs))
                .append(") AGAINST (")/*.append(criteria.isMatchSearch() ? "+" : "-")*/
                // Complete progression matching
                /*.append("\"")*/
                .append(strValue)/*.append("\"")*/ /* Note:: Postgres uses two single quotes */
                .append(" IN BOOLEAN MODE)");
          } else {
            String strValue = safeStringValue(criteria.getValue().toString());
            if (SearchOperation.MATCH.equals(criteria.getOp())) {
              sql.append(" AND ").append(alias).append(".")
                  .append(columnName)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" like \"%%").append(strValue).append("%%\"");
            } else if (SearchOperation.MATCH_END.equals(criteria.getOp())) {
              sql.append(" AND ").append(alias).append(".")
                  .append(columnName)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" like \"").append(strValue).append("%%\"");
            } else if (SearchOperation.NOT_MATCH.equals(criteria.getOp())) {
              sql.append(" AND ").append(alias).append(".")
                  .append(columnName)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" not like \"%%").append(strValue).append("%%\"");
            } else if (SearchOperation.NOT_MATCH_END.equals(criteria.getOp())) {
              sql.append(" AND ").append(alias).append(".")
                  .append(columnName)
                  // Fix:: "%***%" -> "%%***%%": Conversion = '"'; nested exception is java.util.UnknownFormatConversionException: Conversion = '"'
                  .append(" not like \"").append(strValue).append("%%\"");
            }
          }
          hasSearch = true;
        } else if (criteria.isIn()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" in :").append(namingKey);
        } else if (criteria.isNotIn()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" not in :").append(namingKey);
        } else if (criteria.isNull()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" IS NULL ");
        } else if (criteria.isNotNull()) {
          sql.append(" AND ").append(alias).append(".")
              .append(columnName).append(" IS NOT NULL ");
        }
      }
    }
    if (notDeleted && hasDeletedField(step)) {
      sql.append(" AND ").append(alias).append(".deleted_flag = 0");
    }
    if (!isMultiTenantCtrl() || TenantInterceptor.TENANT_TABLES.isEmpty()) {
      return sql;
    }
    // Fix:: Non multi tenant tables are included and duplicate with TenantInterceptor
    // if (PrincipalContext.isOpSysAdmin() && hasOptTenantId()) {
    //  sql.append(" AND ").append(alias).append(".tenant_id = ").append(getOptTenantId());
    // }
    return sql;
  }

  public void setQueryParameter(SingleTableEntityPersister step, Query query,
      Set<SearchCriteria> criterias, Class<?> mainClass) {
    for (SearchCriteria criteria : criterias) {
      if (criteria.isIgnoreFields() || criteria.isNotValidCriteria()) {
        continue;
      }
      String columnName = getColumnName(step, criteria.getKey());
      if (isEmpty(columnName)) {
        continue;
      }
      String namingKey = criteria.getOp().getValue() + criteria.getKey();
      if (SearchOperation.isMatchSearch(criteria)) {
        continue;
      }
      if (SearchOperation.isInSearch(criteria)) {
        Object value = criteria.getValue();
        if (value instanceof String) {
          query.setParameter(namingKey, new HashSet<>(
              Arrays.asList(safeStringInValue(value.toString()).split(","))));
        } else if (value.getClass().isArray()) {
          Object[] array = (Object[]) criteria.getValue();
          query.setParameter(namingKey, List.of(array));
        } else {
          // Such as:
          // value instanceof Collection
          query.setParameter(namingKey, criteria.getValue());
        }
      } else {
        Field f = ReflectionUtils.getField(mainClass, criteria.getKey());
        String strValue = safeStringValue(criteria.getValue().toString());
        if (criteria.getValue() instanceof Value) {
          query.setParameter(namingKey, ((Value) criteria.getValue()).getValue());
        } else if (f.getType().isEnum()) {
          Value<?>[] values = (Value<?>[]) f.getType().getEnumConstants();
          for (Value<?> value : values) {
            if (strValue.equalsIgnoreCase((String) value.getValue())) {
              query.setParameter(namingKey, value.getValue());
            }
          }
        } else if (criteria.getValue() instanceof Boolean) {
          query.setParameter(namingKey, criteria.getValue());
        } else if (isBooleanValue(strValue)) {
          query.setParameter(namingKey, Boolean.valueOf(strValue));
        } else {
          query.setParameter(namingKey, strValue);
        }
      }
    }
  }

  public String getMatchFields(SingleTableEntityPersister step, SearchCriteria criteria,
      String... fifs) {
    ProtocolAssert.assertNotEmpty(fifs, UNSUPPORTED_FILTER_FIELD_T2, UNSUPPORTED_FILTER_FIELD_KEY,
        new Object[]{criteria.getKey(), criteria.getOp().getValue()});
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fifs.length; i++) {
      sb.append(step.getPropertyColumnNames(fifs[i])[0]);
      if (i != fifs.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  public String getAliasMatchFields(SingleTableEntityPersister step, String alias,
      SearchCriteria criteria, String... fifs) {
    // fifs have the highest priority
    if (ObjectUtils.isNotEmpty(fifs)) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < fifs.length; i++) {
        sb.append(alias).append(".").append(step.getPropertyColumnNames(fifs[i])[0]);
        if (i != fifs.length - 1) {
          sb.append(",");
        }
      }
      return sb.toString();
    }

    // Repo overwrite
    String fields = getMatchFields();
    if (ObjectUtils.isNotEmpty(fields)) {
      return fields;
    }
    return getColumnName(step, criteria.getKey());
  }

  public boolean hasDeletedField(SingleTableEntityPersister step) {
    try {
      String[] names = step.getPropertyColumnNames("deletedFlag");
      return nonNull(names) && names.length > 0;
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

  public static String getColumnName(SingleTableEntityPersister step, String filedName) {
    try {
      String[] names = step.getPropertyColumnNames(filedName);
      return nonNull(names) && names.length > 0 ? names[0] : null;
    } catch (Exception e) {
      return null;
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
