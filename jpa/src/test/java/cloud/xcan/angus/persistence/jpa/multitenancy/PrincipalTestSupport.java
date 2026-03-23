package cloud.xcan.angus.persistence.jpa.multitenancy;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import org.junit.jupiter.api.AfterEach;

/** Clears {@link PrincipalContext} after each test. */
abstract class PrincipalTestSupport {

  @AfterEach
  void clearPrincipalContext() {
    PrincipalContext.remove();
  }

  protected static void bindPrincipal(Principal principal) {
    PrincipalContext.set(principal);
  }

  protected static Principal apiTenantPrincipal(long tenantId) {
    return new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(tenantId)
        .setOptTenantId(null);
  }

  protected static Principal apiTenantPrincipal(long tenantId, Long optTenantId) {
    return new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(tenantId)
        .setOptTenantId(optTenantId);
  }

  protected static Principal multiTenantOff(long tenantId) {
    return new Principal()
        .setMultiTenantCtrl(false)
        .setApiType(ApiType.API)
        .setTenantId(tenantId);
  }

  protected static Principal viewNoOptTenant(long tenantId) {
    return new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.VIEW)
        .setTenantId(tenantId)
        .setOptTenantId(null);
  }
}
