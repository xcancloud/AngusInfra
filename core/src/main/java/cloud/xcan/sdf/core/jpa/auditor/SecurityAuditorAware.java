
package cloud.xcan.sdf.core.jpa.auditor;

import cloud.xcan.sdf.spec.principal.PrincipalContext;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;

public class SecurityAuditorAware implements AuditorAware<Long> {

  @NotNull
  @Override
  public Optional<Long> getCurrentAuditor() {
    Long currentUserId = PrincipalContext.getUserId();
    return currentUserId != null && currentUserId > 0L ? Optional.of(currentUserId)
        : Optional.empty();
  }

}
