package cloud.xcan.angus.persistence.jpa.multitenancy;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.function.Supplier;

public class TenantAwareProcessor {

  public void run(Runnable runnable, Long currentTenantId) {
    boolean wasMultiTenantCtrl = isMultiTenantCtrl();
    Long previousOptTenantId = getOptTenantId();
    try {
      if (wasMultiTenantCtrl) {
        PrincipalContext.get().setMultiTenantCtrl(false);
        if (nonNull(currentTenantId)) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }
      }
      runnable.run();
    } finally {
      if (wasMultiTenantCtrl) {
        PrincipalContext.get().setMultiTenantCtrl(true);
        if (nonNull(currentTenantId)) {
          PrincipalContext.get().setOptTenantId(previousOptTenantId);
        }
      }
    }
  }

  public <R> R call(Supplier<R> supplier, Long currentTenantId) {
    boolean wasMultiTenantCtrl = isMultiTenantCtrl();
    Long previousOptTenantId = getOptTenantId();
    try {
      if (wasMultiTenantCtrl) {
        PrincipalContext.get().setMultiTenantCtrl(false);
        if (nonNull(currentTenantId)) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }
      }
      return supplier.get();
    } finally {
      if (wasMultiTenantCtrl) {
        PrincipalContext.get().setMultiTenantCtrl(true);
        if (nonNull(currentTenantId)) {
          PrincipalContext.get().setOptTenantId(previousOptTenantId);
        }
      }
    }
  }
}
