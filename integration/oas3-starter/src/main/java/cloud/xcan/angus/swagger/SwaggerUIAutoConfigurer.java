package cloud.xcan.angus.swagger;

import jakarta.annotation.Resource;
import java.util.List;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnProperty(name = {"springdoc.swagger-ui.enabled"}, matchIfMissing = true)
//@EnableConfigurationProperties({SwaggerUiConfigProperties.class})
public class SwaggerUIAutoConfigurer implements WebMvcConfigurer {

  public static final String[] DEFAULT_SUBMIT_METHODS = new String[]{
      "get", "put", "post", "delete", "options", "head", "patch", "trace"};

  @Value("${server.servlet.context-path:'/'}")
  private String contextPath;

  @Resource
  private WebProperties webProperties;

  public SwaggerUIAutoConfigurer() {
  }

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

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String baseUrl = StringUtils.trimTrailingCharacter(contextPath, '/');
    registry.addResourceHandler("/**")
        .addResourceLocations(webProperties.getResources().getStaticLocations());
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    registry.addResourceHandler(baseUrl + "swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController(contextPath + "swagger-ui/")
        .setViewName("forward:" + contextPath + "swagger-ui/index.html");
  }

}
