
package cloud.xcan.sdf.web.swagger;

import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ObjectUtils;

@Configuration
@EnableConfigurationProperties({SwaggerProperties.class, ApplicationInfo.class})
@Conditional({SwaggerAutoConfigurer.SwaggerCondition.class})
@Import(SwaggerUiMvcConfigurer.class)
public class SwaggerAutoConfigurer {

  static final class SwaggerCondition implements Condition {

    SwaggerCondition() {
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata a) {
      String enabled = context.getEnvironment().getProperty("xcan.swagger.enabled");
      return !ObjectUtils.isEmpty(enabled) && Boolean.parseBoolean(enabled);
    }
  }
}
