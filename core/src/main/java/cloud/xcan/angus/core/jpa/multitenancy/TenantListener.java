package cloud.xcan.angus.core.jpa.multitenancy;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.decideMultiTenantCtrlByApiType;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;

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
    if (!decideMultiTenantCtrlByApiType(principal)
        || !isMultiTenantCtrl(principal)) {
      return;
    }
    // Fix:: The operation administrator operates himself tenant
    //      Long opTenantId = getRealOptTenantId(principal);
    //      if (isTopUser() && isNull(opTenantId)) {
    //        return sql;
    //      }
    entity.setTenantId(getOptTenantId(principal));
  }

}
