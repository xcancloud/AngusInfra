package cloud.xcan.angus.core.jpa.multitenancy;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.decideMultiTenantCtrlByApiType;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;

import cloud.xcan.angus.core.jpa.interceptor.TenantInterceptor;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

public class TenantListener {

  /**
   * Do not do multi-tenant control (multiTenantCtrl=false or not auth api) or the operation
   * administrator but does not set the operation tenant optTenantId.
   *
   * @see TenantInterceptor
   */
  @PrePersist
  @PreUpdate
  @PreRemove
  public void beforeAnyUpdate(TenantAware entity) {
    Principal principal = PrincipalContext.get();
    // Multi-tenancy control is disabled: Users must manually manage multi-tenant data isolation, including adding tenant ID conditions in SQL statements.
    if (!principal.isMultiTenantCtrl() || !decideMultiTenantCtrlByApiType(principal)) {
      return;
    }
    entity.setTenantId(getOptTenantId(principal));
  }

}
