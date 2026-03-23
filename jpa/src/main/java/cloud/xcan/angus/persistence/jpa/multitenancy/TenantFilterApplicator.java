package cloud.xcan.angus.persistence.jpa.multitenancy;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.decideMultiTenantCtrlByApiType;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isValidOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.experimental.MultiTenant;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import org.hibernate.Filter;
import org.hibernate.Session;

/**
 * Binds Hibernate {@value TenantFilterNames#SCOPE} on the current persistence context session when
 * {@link Principal} flags and API type require automatic tenant scoping. JPA/HQL/Criteria queries
 * on {@link MultiTenant} mapped types then receive {@code AND tenant_id = :tenantId} at the SQL
 * generation layer (not via fragile string rewriting).
 */
public final class TenantFilterApplicator {

  private TenantFilterApplicator() {
  }

  public static boolean shouldApplyTenantFilter(Principal principal) {
    if (principal == null) {
      return false;
    }
    return principal.isMultiTenantCtrl()
        && decideMultiTenantCtrlByApiType(principal)
        && !isEmpty(TenantMetadata.getTenantTableNames());
  }

  /**
   * Enables or disables the tenant filter for this Hibernate {@link Session} according to the
   * current {@link PrincipalContext}. Safe to call multiple times per transaction.
   */
  public static void syncSession(Session session) {
    if (session == null) {
      return;
    }
    Filter enabled = session.getEnabledFilter(TenantFilterNames.SCOPE);
    if (enabled != null) {
      session.disableFilter(TenantFilterNames.SCOPE);
    }
    Principal principal = PrincipalContext.get();
    if (!shouldApplyTenantFilter(principal)) {
      return;
    }
    Long tenantId = getOptTenantId(principal);
    if (!isValidOptTenantId(tenantId)) {
      throw new IllegalStateException(
          "Multi-tenant filtering is required but optTenantId/tenantId is missing or invalid");
    }
    session.enableFilter(TenantFilterNames.SCOPE)
        .setParameter(TenantFilterNames.PARAM_TENANT_ID, tenantId);
  }
}
