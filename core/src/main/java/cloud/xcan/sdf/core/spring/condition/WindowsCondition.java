package cloud.xcan.sdf.core.spring.condition;

import cloud.xcan.sdf.spec.Platform;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class WindowsCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return Platform.isWindows();
  }
}
