package cloud.xcan.angus.core.spring.condition;

import cloud.xcan.angus.api.enums.SupportedDbType;
import cloud.xcan.angus.spec.experimental.Assert;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PostgresEnvCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String dbType = context.getEnvironment().getProperty("xcan.datasource.extra.dbType");
    Assert.assertNotEmpty(dbType, "dbType config is required");
    return SupportedDbType.POSTGRES.getValue().equalsIgnoreCase(dbType);
  }
}
