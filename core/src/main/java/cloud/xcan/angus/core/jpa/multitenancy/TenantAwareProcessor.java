package cloud.xcan.angus.core.jpa.multitenancy;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;

import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.function.Supplier;

public class TenantAwareProcessor {

  public void run(Runnable runnable) {
    boolean isMultiTenantCtrl = isMultiTenantCtrl();
    long realTenantId = getOptTenantId();
    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(false);
      PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
    }

    runnable.run();

    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(true);
      PrincipalContext.get().setOptTenantId(realTenantId);
    }
  }

  public <R> R call(Supplier<R> supplier) {
    boolean isMultiTenantCtrl = isMultiTenantCtrl();
    long realTenantId = getOptTenantId();
    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(false);
      PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
    }

    R result = supplier.get();

    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(true);
      PrincipalContext.get().setOptTenantId(realTenantId);
    }
    return result;
  }


}
