package cloud.xcan.sdf.core.spring.condition;

import cloud.xcan.sdf.api.enums.SupportedDbType;
import cloud.xcan.sdf.spec.experimental.Assert;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MySqlEnvCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String dbType = context.getEnvironment().getProperty("xcan.datasource.extra.dbType");
    Assert.assertNotEmpty(dbType, "dbType config is required");
    return SupportedDbType.MYSQL.getValue().equalsIgnoreCase(dbType);
  }
}
