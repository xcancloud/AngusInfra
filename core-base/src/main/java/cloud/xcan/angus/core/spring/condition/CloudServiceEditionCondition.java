package cloud.xcan.angus.core.spring.condition;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.spec.annotations.CloudServiceEdition;
import cloud.xcan.angus.spec.experimental.Assert;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@CloudServiceEdition
public class CloudServiceEditionCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String editionType = context.getEnvironment().getProperty("info.app.editionType");
    Assert.assertNotEmpty(editionType, "editionType config is required");
    return EditionType.CLOUD_SERVICE.getValue().equalsIgnoreCase(editionType);
  }
}
