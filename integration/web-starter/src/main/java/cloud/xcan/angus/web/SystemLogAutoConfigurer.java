package cloud.xcan.angus.web;


import static cloud.xcan.angus.web.endpoint.SystemLogEndpoint.FILE_PATH_PROPERTY;

import cloud.xcan.angus.web.endpoint.SystemLogEndpoint;
import cloud.xcan.angus.core.log.SystemLogProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SystemLogProperties.class)
@ConditionalOnProperty(prefix = "xcan.syslog", name = "enabled", matchIfMissing = true)
public class SystemLogAutoConfigurer {

  private static String path;

  @Bean
  @ConditionalOnMissingBean
  @Conditional(SystemLogCondition.class)
  public SystemLogEndpoint systemLogEndpoint() {
    return new SystemLogEndpoint(path);
  }

  private static class SystemLogCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
        AnnotatedTypeMetadata metadata) {
      Environment environment = context.getEnvironment();
      ConditionMessage.Builder message = ConditionMessage.forCondition("SystemRequest File");
      path = getLogFileConfig(environment);
      if (StringUtils.hasText(path)) {
        return ConditionOutcome.match(message.found(FILE_PATH_PROPERTY).items(path));
      }
      return ConditionOutcome.noMatch(message.didNotFind("logging file").atAll());
    }

    private String getLogFileConfig(Environment environment) {
      return environment.resolvePlaceholders("${" + FILE_PATH_PROPERTY + ":}");
    }
  }

}
