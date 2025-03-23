package cloud.xcan.angus.swagger;

import java.util.List;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"springdoc.swagger-ui.enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({SwaggerUiConfigProperties.class})
public class SwaggerUIAutoConfigurer {

  public static final String[] DEFAULT_SUBMIT_METHODS = new String[]{
      "get", "put", "post", "delete", "options", "head", "patch", "trace"};

  @Bean
  public SwaggerUiConfigParameters swaggerUiConfigParameters(
      SwaggerUiConfigProperties configProperties /* Yaml file properties */) {

    SwaggerUiConfigParameters configParameters = new SwaggerUiConfigParameters(configProperties);
    configParameters.setDeepLinking(true);
    configParameters.setDisplayOperationId(true);
    configParameters.setDefaultModelExpandDepth(2);
    configParameters.setDefaultModelsExpandDepth(2);
    configParameters.setDefaultModelRendering("example"/*or model*/);
    configParameters.setDisplayRequestDuration(true);
    configParameters.setTagsSorter("alpha");
    configParameters.setOperationsSorter("method");
    configParameters.setShowExtensions(true);
    configParameters.setShowCommonExtensions(true);
    configParameters.setSupportedSubmitMethods(List.of(DEFAULT_SUBMIT_METHODS));
    return configParameters;
  }

}
