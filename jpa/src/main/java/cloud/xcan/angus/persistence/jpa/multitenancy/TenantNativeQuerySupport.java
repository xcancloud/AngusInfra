package cloud.xcan.angus.persistence.jpa.multitenancy;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasOptTenantId;
import static cloud.xcan.angus.spec.experimental.BizConstant.TENANT_ID_DB_KEY;

import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.persistence.EntityType;
import jakarta.persistence.Query;

/**
 * Native SQL bypasses Hibernate {@code @Filter}; when automatic tenant scoping is on, callers
 * append {@code tenant_id = :xcanTenantId} and bind via {@link #bindTenantParameterIfNeeded}.
 */
public final class TenantNativeQuerySupport {

  public static final String PARAM_NAME = "xcanTenantId";

  private TenantNativeQuerySupport() {
  }

  /**
   * When automatic tenant isolation applies to this entity type, a valid tenant id is required
   * (same fail-closed rule as {@link TenantFilterApplicator#syncSession}).
   */
  public static boolean shouldAppendTenantPredicate(Class<?> entityClass) {
    if (entityClass == null || !TenantMetadata.isMultiTenantEntity(entityClass)) {
      return false;
    }
    if (!TenantFilterApplicator.shouldApplyTenantFilter(PrincipalContext.get())) {
      return false;
    }
    if (!hasOptTenantId()) {
      throw new IllegalStateException(
          "Multi-tenant filtering is required for this query but optTenantId/tenantId is missing or invalid");
    }
    return true;
  }

  public static boolean shouldAppendForManagedType(EntityType<?> entityType) {
    return entityType != null && shouldAppendTenantPredicate(entityType.getJavaType());
  }

  /** {@code AND alias.tenant_id = :xcanTenantId} (SQL fragment only). */
  public static void appendQualifiedTenantClause(StringBuilder sql, String alias,
      Class<?> entityClass) {
    if (!shouldAppendTenantPredicate(entityClass)) {
      return;
    }
    sql.append(" AND ").append(alias).append(".").append(TENANT_ID_DB_KEY).append(" = :")
        .append(PARAM_NAME);
  }

  /** {@code AND tenant_id = :xcanTenantId} without table prefix (SQL fragment only). */
  public static void appendUnqualifiedTenantClause(StringBuilder sql, Class<?> entityClass) {
    if (!shouldAppendTenantPredicate(entityClass)) {
      return;
    }
    sql.append(" AND ").append(TENANT_ID_DB_KEY).append(" = :").append(PARAM_NAME);
  }

  public static void bindTenantParameterIfNeeded(Query query, Class<?> entityClass) {
    if (!shouldAppendTenantPredicate(entityClass)) {
      return;
    }
    query.setParameter(PARAM_NAME, getOptTenantId(PrincipalContext.get()));
  }
}
