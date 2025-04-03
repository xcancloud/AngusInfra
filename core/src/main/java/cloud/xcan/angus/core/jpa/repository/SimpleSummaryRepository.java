package cloud.xcan.angus.core.jpa.repository;

import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationClasses;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasRealOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasToRole;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isOpSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isToUser;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.setMultiTenantCtrl;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.repository.summary.Aggregate;
import cloud.xcan.angus.core.jpa.repository.summary.DateRangeType;
import cloud.xcan.angus.core.jpa.repository.summary.GroupBy;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryBuilder;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * ################### SimpleSummaryRepository ###################
 *
 * ----- The exact value (such as: ID, DATE and other columns) statistics are not supported! ------
 *
 * ## Mode 1 (no grouping GroupBy=null) - Default statistics
 * SELECT
 * 	COUNT(id)
 * FROM
 * 	`user`
 * WHERE
 * 	tenant_id = 1
 * 	AND created_date >= "2022-01-19 00:00:00"
 *
 * ------------ Results ----------
 * -- COUNT(id)
 * -- ---
 * -- 21
 * ------------ Model ------------
 * Map<String,Long> :: COUNT_id -> summary
 *
 * ## Mode 2 (GroupBy=STATUS) - status column grouping statistics (multiple status column statistics are supported)
 * SELECT
 * 	source, gender, admin_flag, enabled, locked, COUNT(id)
 * FROM
 * 	`user`
 * WHERE
 * 	tenant_id = 1
 * 	AND created_date >= "2022-01-19 00:00:00"
 * GROUP BY
 * 	source, gender, admin_flag, enabled, locked;
 *
 * ------------ Results ----------
 * -- source|gender|admin_flag|enabled|locked|COUNT(id)
 * -- ----------------------
 * -- BACKGROUND_ADDED	MALE	0	0	1	1
 * -- BACKGROUND_ADDED	MALE	0	1	0	14
 * -- BACKGROUND_ADDED	MALE	1	1	0	4
 * -- INVITATION_CODE_SIGNUP	UNKNOWN	0	1	0	1
 * -- INVITATION_CODE_SIGNUP	UNKNOWN	1	1	0	1
 * ------------ Model ------------
 * Map<String,Map<String,Map<String,Long>>> :: source -> status... -> COUNT_id + TOTAL -> status... + TOTAL summary
 *
 * ## Mode 3 (GroupBy=DATE) - Time column range grouping statistics (only single date column statistics are supported)
 *
 * SELECT
 * 	DATE_FORMAT(created_date, '%Y-%u') times, COUNT(id)
 * FROM
 * 	`user`
 * WHERE
 * 	tenant_id = 1
 * 	AND created_date >= "2022-01-19 00:00:00"
 * GROUP BY
 * 	times;
 *
 * ------------ Results ----------
 * -- times|COUNT(id)
 * -- ----------------------
 * -- 2022-13	11
 * -- 2022-14	3
 * -- 2022-15	2
 * -- 2022-18	1
 * -- 2022-25	1
 * -- 2022-26	1
 * -- 2022-33	1
 * -- 2022-35	1
 * ------------ Model ------------
 * Map<String,Map<String,Long>> :: times -> COUNT_id -> summary
 *
 * </pre>
 */
public class SimpleSummaryRepository implements SummaryRepository {

  public static Map<String, SummaryQueryRegister> REGISTER = new HashMap<>();

  @Resource
  @PersistenceContext
  public EntityManager entityManager;

  public SimpleSummaryRepository() {
    if (CollectionUtils.isEmpty(REGISTER)) {
      REGISTER = loadAnnotation("cloud.xcan.angus", SummaryQueryRegister.class);
    }
  }

  @Override
  public List<Object[]> getSummer(SummaryQueryBuilder builder) {
    SummaryQueryRegister register = REGISTER.get(builder.getName());
    setMultiTenantCtrl(register.isMultiTenantCtrl() || !hasMultiTenantAuthority(builder));
    Query queryList = entityManager.createNativeQuery(getSummarySql(builder));
    return (List<Object[]>) queryList.getResultList();
  }

  @DoInFuture("Postgres support")
  public String getSummarySql(SummaryQueryBuilder builder) {
    SummaryQueryRegister register = REGISTER.get(builder.getName());
    StringBuilder sql = new StringBuilder();
    GroupBy groupBy = builder.getGroupBy();
    // Has group -> Group and aggregate
    if (Objects.nonNull(groupBy)) {
      if (groupBy.isDateRange()) {
        DateRangeType type = builder.getDateRangeType();
        // Only one column when GroupBy.DATE
        sql.append("SELECT DATE_FORMAT(").append(register.groupByColumns()[0])
            .append(",'").append(type.toFormat()).append("') times, ")
            .append(getAggregateCondition(builder.getAggregates()))
            .append(" FROM ")
            .append(register.table())
            .append(" WHERE 1=1").append(getMultiTenantCondition(register))
            .append(getCriteriaCondition(builder.getFilters()))
            .append(getDeletedCondition(register))
            .append(" GROUP BY times ");
      } else {
        // Allow multi-column when GroupBy.STATUS
        String groupByColumns = getGroupByColumns(builder.getGroupByColumns());
        sql.append("SELECT ").append(groupByColumns)
            .append(" , ").append(getAggregateCondition(builder.getAggregates()))
            .append(" FROM ")
            .append(register.table())
            .append(" WHERE 1=1").append(getMultiTenantCondition(register))
            .append(getCriteriaCondition(builder.getFilters()))
            .append(getDeletedCondition(register))
            .append(" GROUP BY ").append(groupByColumns);
      }
    } else {
      // No group -> Only aggregate
      sql.append("SELECT ").append(getAggregateCondition(builder.getAggregates()))
          .append(" FROM ")
          .append(register.table())
          .append(" WHERE 1=1").append(getMultiTenantCondition(register))
          .append(getCriteriaCondition(builder.getFilters()))
          .append(getDeletedCondition(register));
    }
    return sql.toString();
  }

  private String getMultiTenantCondition(SummaryQueryRegister register) {
    return register.isMultiTenantCtrl() ? judgeAutoCtrlTenantIdCondition(register) : "";
  }

  /**
   * Query all when the operation client and optTenantId is null
   */
  private String judgeAutoCtrlTenantIdCondition(SummaryQueryRegister register) {
    if (!register.multiTenantAutoCtrlWhenOpClient() || !isToUser() || hasRealOptTenantId()) {
      return " AND tenant_id = " + getOptTenantId();
    }
    // Close TenantInterceptor tenantId condition
    setMultiTenantCtrl(false);
    return "";
  }

  private String getDeletedCondition(SummaryQueryRegister register) {
    return register.ignoreDeleted() ? " AND deleted = 0 " : "";
  }

  private String getAggregateCondition(List<Aggregate> aggregates) {
    StringBuilder builder = new StringBuilder();
    for (Aggregate aggregate : aggregates) {
      builder.append(aggregate.getFunction()).append("(").append(aggregate.getColumn())
          .append("),");
    }
    return StringUtils.removeEnd(builder.toString(), ",");
  }

  private String getGroupByColumns(List<String> groupByColumns) {
    return String.join(",", groupByColumns);
  }

  private boolean hasMultiTenantAuthority(SummaryQueryBuilder builder) {
    return isOpSysAdmin() || hasToRole(REGISTER.get(builder.getName()).topAuthority());
  }

  private String getCriteriaCondition(Set<SearchCriteria> criterias) {
    StringBuilder builder = new StringBuilder();
    if (isNotEmpty(criterias)) {
      for (SearchCriteria criteria : criterias) {
        if (criteria.isValidCriteria()) {
          builder.append(" AND ").append(criteria.toConditionString(""));
        }
      }
    }
    return builder.toString();
  }

  public static Map<String, SummaryQueryRegister> loadAnnotation(String packageName,
      Class<? extends Annotation> annotation) {
    Set<Class<?>> allClazz = getAnnotationClasses(packageName, annotation);
    Map<String, SummaryQueryRegister> registerMap = new HashMap<>();
    for (Class<?> c : allClazz) {
      SummaryQueryRegister register = (SummaryQueryRegister) c
          .getAnnotation(SummaryQueryRegister.class);
      if (Objects.nonNull(register)) {
        registerMap.put(register.name(), register);
      }
    }
    return registerMap;
  }

  public static boolean hasSummaryResource(String name) {
    return Objects.nonNull(REGISTER.get(name));
  }
}
