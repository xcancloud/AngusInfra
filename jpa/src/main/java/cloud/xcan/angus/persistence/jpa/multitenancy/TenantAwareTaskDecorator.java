package cloud.xcan.angus.persistence.jpa.multitenancy;

import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * Propagates a {@link Principal} snapshot to pooled async threads (where
 * {@link PrincipalContext#threadLocal} is not inherited). Restores or clears the previous thread
 * binding in {@code finally} to avoid leaking context across tasks.
 */
public class TenantAwareTaskDecorator implements TaskDecorator {

  @Override
  @NonNull
  public Runnable decorate(@NonNull Runnable runnable) {
    Principal captured = PrincipalContext.threadLocal.get();
    if (captured == null) {
      return runnable;
    }
    Principal snapshot = copyForAsyncPropagation(captured);
    return () -> {
      Principal previous = PrincipalContext.threadLocal.get();
      try {
        PrincipalContext.set(snapshot);
        runnable.run();
      } finally {
        if (previous != null) {
          PrincipalContext.set(previous);
        } else {
          PrincipalContext.remove();
        }
      }
    };
  }

  static Principal copyForAsyncPropagation(Principal source) {
    Principal snapshot = new Principal();
    BeanUtils.copyProperties(source, snapshot);
    Map<String, Object> ext = source.getExtensions();
    if (ext != null && !ext.isEmpty()) {
      snapshot.setExtensions(new HashMap<>(ext));
    }
    List<String> permissions = source.getPermissions();
    if (permissions != null) {
      snapshot.setPermissions(List.copyOf(permissions));
    }
    return snapshot;
  }
}
