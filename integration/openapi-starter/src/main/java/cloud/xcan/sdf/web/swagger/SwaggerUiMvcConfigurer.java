
package cloud.xcan.sdf.web.swagger;

import static cloud.xcan.sdf.api.ApiConstant.API_KEY_NAME;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.TOKEN_TYPE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.API_KEY;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.AUTHORIZATION;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.OPT_TENANT_ID;
import static java.util.Collections.singletonList;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;

import cloud.xcan.sdf.api.ApiResult;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.sdf.core.spring.condition.PrivateEditionCondition;
import cloud.xcan.sdf.spec.annotations.CloudServiceEdition;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author liuxiaolong
 */
@Slf4j
@EnableSwagger2
@ConditionalOnClass(WebMvcConfigurer.class)
@Configuration
public class SwaggerUiMvcConfigurer implements WebMvcConfigurer {

  public static final String[] DEFAULT_SUBMIT_ALPHAS = new String[]{
      "get", "put", "post", "delete", "head", "patch"};

  @Value("${server.servlet.context-path:'/'}")
  private String contextPath;

  @Value("${spring.application.name}")
  private String appName;

  @Resource
  private WebProperties wp;

  @Resource
  private SwaggerProperties sp;

  private final Environment environment;

  public SwaggerUiMvcConfigurer(Environment environment) {
    this.environment = environment;
  }

  @Bean
  UiConfiguration uiConfig() {
    return UiConfigurationBuilder.builder()
        .deepLinking(true)
        .displayOperationId(true)
        .defaultModelsExpandDepth(1)
        .defaultModelExpandDepth(1)
        .defaultModelRendering(ModelRendering.EXAMPLE)
        .displayRequestDuration(true)
        .docExpansion(DocExpansion.NONE)
        .filter(false)
        .maxDisplayedTags(null)
        .operationsSorter(OperationsSorter.ALPHA)
        .showExtensions(true)
        .showCommonExtensions(true)
        .tagsSorter(TagsSorter.ALPHA)
        .supportedSubmitMethods(DEFAULT_SUBMIT_ALPHAS)
        .validatorUrl(null)
        .build();
  }

  @Bean
  Docket docketApiV1(@Qualifier("globalParameters") List<RequestParameter> globalParameters,
      ApplicationInfo applicationInfo) {
    Docket docket;
    if (applicationInfo.isPrivateEdition()) {
      docket = new Docket(DocumentationType.SWAGGER_2).select()
          .apis(withMethodAnnotation(ApiOperation.class))
          //.apis(Predicate.not(withMethodAnnotation(OperationClient.class)))
          .apis(Predicate.not(withMethodAnnotation(CloudServiceEdition.class)))
          .paths(PathSelectors.ant("/api/v1/**"))
          .build();
    } else {
      // Cloud service edition
      docket = new Docket(DocumentationType.SWAGGER_2).select()
          .apis(withMethodAnnotation(ApiOperation.class))
          .paths(PathSelectors.ant("/api/v1/**"))
          .build();
    }
    return docket.securityContexts(singletonList(apiSecurityContexts()))
        .securitySchemes(singletonList(apiSecuritySchemes()))
        .apiInfo(apiInfo())
        .groupName("Api")
        .useDefaultResponseMessages(false)
        .genericModelSubstitutes(ApiResult.class)
        .globalRequestParameters(globalParameters)
        //.globalRequestParameters(globalLocaleParameters())
        .globalResponses(org.springframework.http.HttpMethod.POST, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.PATCH, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.PUT, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.DELETE, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.GET, globalAuthResponses());
  }

  @Bean
  @Conditional(value = CloudServiceEditionCondition.class)
  Docket docketOpenApiV1() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(withMethodAnnotation(ApiOperation.class))
        .paths(PathSelectors.ant("/openapi2p/v1/**"))  // token Auth
        .build()
        .securityContexts(singletonList(apiSecurityContexts()))
        .securitySchemes(singletonList(apiSecuritySchemes()))
        .apiInfo(apiInfo())
        .groupName("Open Api to Private")
        .useDefaultResponseMessages(false)
        .genericModelSubstitutes(ApiResult.class)
        //.globalRequestParameters(globalLocaleParameters())
        .globalResponses(org.springframework.http.HttpMethod.POST, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.PATCH, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.PUT, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.DELETE, globalAuthResponses())
        .globalResponses(org.springframework.http.HttpMethod.GET, globalAuthResponses());
  }

  //  @Bean
  //  Docket docketOpenApiV1() {
  //    return new Docket(DocumentationType.SWAGGER_2)
  //        .select()
  //        .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
  //        .paths(PathSelectors.ant("/openapi/v1/**")) // apiKey Auth
  //        .build()
  //        .securityContexts(Collections.singletonList(openapiSecurityContext()))
  //        .securitySchemes(Collections.singletonList(openapiSecuritySchemes()))
  //        .apiInfo(apiInfo())
  //        .groupName("Open Api")
  //        .useDefaultResponseMessages(false)
  //        .genericModelSubstitutes(ApiResult.class)
  //        .globalResponses(HttpMethod.POST, globalAuthResponses())
  //        .globalResponses(HttpMethod.PATCH, globalAuthResponses())
  //        .globalResponses(HttpMethod.PUT, globalAuthResponses())
  //        .globalResponses(HttpMethod.DELETE, globalAuthResponses())
  //        .globalResponses(HttpMethod.GET, globalAuthResponses());
  //  }

  @Bean
  Docket docketPubapiV1() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(withMethodAnnotation(ApiOperation.class))
        .paths(PathSelectors.ant("/pubapi/v1/**"))
        .build()
        .apiInfo(apiInfo())
        .groupName("Public Api")
        .useDefaultResponseMessages(false)
        .genericModelSubstitutes(ApiResult.class)
        //.globalRequestParameters(globalLocaleParameters())
        .globalResponses(org.springframework.http.HttpMethod.POST, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.PATCH, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.PUT, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.DELETE, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.GET, globalResponses());
  }

  /*@Bean
  Docket docketPubviewV1() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(withMethodAnnotation(ApiOperation.class))
        .paths(PathSelectors.ant("/pubview/v1/**"))
        .build()
        .apiInfo(apiInfo())
        .groupName("Public View")
        .useDefaultResponseMessages(false)
        //.genericModelSubstitutes(ApiResult.class)
        //.globalRequestParameters(globalLocaleParameters())
        //.globalResponses(org.springframework.http.HttpMethod.POST, globalResponses())
        //.globalResponses(org.springframework.http.HttpMethod.PATCH, globalResponses())
        //.globalResponses(org.springframework.http.HttpMethod.PUT, globalResponses())
        //.globalResponses(org.springframework.http.HttpMethod.DELETE, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.GET, globalResponses());
  }*/

  @Bean
  @Conditional(value = CloudServiceEditionCondition.class)
  Docket docketDoorapiV1(@Qualifier("globalParameters") List<RequestParameter> globalParameters) {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(withMethodAnnotation(ApiOperation.class))
        .paths(PathSelectors.ant("/doorapi/v1/**"))
        .build()
        .apiInfo(apiInfo())
        .groupName("Door Api")
        .useDefaultResponseMessages(false)
        .genericModelSubstitutes(ApiResult.class)
        .globalRequestParameters(globalParameters)
        //.globalRequestParameters(globalLocaleParameters())
        .globalResponses(org.springframework.http.HttpMethod.POST, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.PATCH, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.PUT, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.DELETE, globalResponses())
        .globalResponses(org.springframework.http.HttpMethod.GET, globalResponses());
  }

  List<Response> globalResponses() {
    List<Response> responses = new ArrayList<>(4);
    responses.add(new ResponseBuilder()
        .code("400")
        .description("Bad Request (client error)")
        .build());

    responses.add(new ResponseBuilder()
        .code("405")
        .description("HttpMethod not allowed (client error)")
        .build());

    responses.add(new ResponseBuilder()
        .code("415")
        .description("Unsupported media type (client error)")
        .build());

    responses.add(new ResponseBuilder()
        .code("500")
        .description("Internal server error (server error)")
        .build());

    return responses;
  }

  List<Response> globalAuthResponses() {
    List<Response> responses = new ArrayList<>(6);
    responses.addAll(globalResponses());
    responses.add(new ResponseBuilder()
        .code("401")
        .description("Unauthorized (client error)")
        .build());

    responses.add(new ResponseBuilder()
        .code("403")
        .description("Forbidden (client error)")
        .build());

    return responses;
  }

  //  private List<RequestParameter> globalLocaleParameters() {
  //    List<RequestParameter> parameters = new ArrayList<>();
  //    parameters.add(new RequestParameterBuilder()
  //        .name(LOCALE_COOKIE_NAME)
  //        .description("Locale. eg:zh-CN, allowable values: zh-CN,en")
  //        .in(ParameterType.COOKIE)
  //        .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
  //        .required(false)
  //        .build());
  //    return parameters;
  //  }

  @Conditional(value = CloudServiceEditionCondition.class)
  @Bean("globalParameters")
  public List<RequestParameter> globalOptTenantParameters() {
    List<RequestParameter> parameters = new ArrayList<>();
    parameters.add(new RequestParameterBuilder()
        .name(OPT_TENANT_ID)
        .description("Operation tenant ID")
        .in(ParameterType.HEADER)
        .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
        .required(false)
        .build());
    return parameters;
  }

  @Conditional(value = PrivateEditionCondition.class)
  @Bean("globalParameters")
  public List<RequestParameter> globalParameters() {
    return new ArrayList<>();
  }

  private ApiKey openapiSecuritySchemes() {
    return new ApiKey(API_KEY_NAME, API_KEY, ParameterType.HEADER.getIn());
  }

  private SecurityContext openapiSecurityContext() {
    return SecurityContext.builder()
        .securityReferences(openapiAuthorization())
        .forPaths(PathSelectors.any())
        .build();
  }

  private List<SecurityReference> openapiAuthorization() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "access everything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return singletonList(new SecurityReference("apiKey", authorizationScopes));
  }

  private SecurityScheme apiSecuritySchemes() {
    return new ApiKey(TOKEN_TYPE, AUTHORIZATION, ParameterType.HEADER.getIn());
  }

  private SecurityContext apiSecurityContexts() {
    return SecurityContext.builder()
        .securityReferences(apiAuthorization())
        .forPaths(PathSelectors.any())
        .build();
  }

  private List<SecurityReference> apiAuthorization() {
    AuthorizationScope[] authScopes = new AuthorizationScope[1];
    authScopes[0] = new AuthorizationScope("global", "access everything");
    return singletonList(new SecurityReference(TOKEN_TYPE, authScopes));
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title(appName)
        .description(sp.getDescription())
        .version(sp.getVersion())
        .license(sp.getLicense())
        .licenseUrl(sp.getLicenseUrl())
        .contact(new Contact(sp.getContact().getName(), sp.getContact().getUrl(),
            sp.getContact().getEmail()))
        .build();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String baseUrl = StringUtils.trimTrailingCharacter(this.contextPath, '/');
    registry.addResourceHandler("/**")
        .addResourceLocations(wp.getResources().getStaticLocations());
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    registry.addResourceHandler(baseUrl + "swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        .resourceChain(false);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController(contextPath + "swagger-ui/")
        .setViewName("forward:" + contextPath + "swagger-ui/index.html");
  }
}
