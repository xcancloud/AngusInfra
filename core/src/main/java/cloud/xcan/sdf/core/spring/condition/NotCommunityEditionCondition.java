package cloud.xcan.sdf.core.spring.condition;

import cloud.xcan.sdf.api.enums.EditionType;
import cloud.xcan.sdf.spec.annotations.PrivateEdition;
import cloud.xcan.sdf.spec.experimental.Assert;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@PrivateEdition
public class NotCommunityEditionCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String editionType = context.getEnvironment().getProperty("info.app.editionType");
    Assert.assertNotEmpty(editionType, "editionType config is required");
    return !EditionType.COMMUNITY.getValue().equalsIgnoreCase(editionType);
  }
}
