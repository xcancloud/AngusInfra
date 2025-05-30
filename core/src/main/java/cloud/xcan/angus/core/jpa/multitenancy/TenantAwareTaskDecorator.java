package cloud.xcan.angus.core.jpa.multitenancy;

import cloud.xcan.angus.spec.principal.PrincipalContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class TenantAwareTaskDecorator implements TaskDecorator {

  @Override
  @NonNull
  public Runnable decorate(@NonNull Runnable runnable) {
    Long tenantId = PrincipalContext.getTenantId();
    return () -> {
      try {
        PrincipalContext.get().setTenantId(tenantId);
        runnable.run();
      } finally {
        PrincipalContext.get().setTenantId(null);
      }
    };
  }
}
