package cloud.xcan.angus.core.spring.condition;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.spec.annotations.PrivateEdition;
import cloud.xcan.angus.spec.experimental.Assert;
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
