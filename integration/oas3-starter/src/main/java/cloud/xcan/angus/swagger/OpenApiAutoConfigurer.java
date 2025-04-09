package cloud.xcan.angus.swagger;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_SYS_HTTP_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_SYS_OAUTH2_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_USER_HTTP_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_USER_OAUTH2_NAME;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.spec.annotations.CloudServiceEdition;
import cloud.xcan.angus.spec.annotations.PrivateEdition;
import cloud.xcan.angus.spec.experimental.Assert;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"springdoc.api-docs.enabled"}, matchIfMissing = true)
//@EnableConfigurationProperties({SpringDocConfigProperties.class})
public class OpenApiAutoConfigurer {

  @Value("${springdoc.oauth2.token-url: http://localhost:9090/oauth2/token}")
  private String oauth2TokenUrl;

  @Bean
  public OpenAPI openAPI(SpringDocConfigProperties doc) {
    OpenAPI openAPI = doc.getOpenApi();
    Assert.assertNotNull(openAPI, "OpenAPI config should not be null");
    return openAPI;
  }

  @Bean
  public GroupedOpenApi userApi(ApplicationInfo applicationInfo) {
    GroupedOpenApi openApi;
    if (applicationInfo.isPrivateEdition()) {
      // Private edition
      openApi = GroupedOpenApi.builder()
          .displayName("/api (Private Edition User Api Document)")
          .group("user")
          .pathsToMatch("/api/v1/**")
          // Exclude cloud service edition apis
          .addOpenApiMethodFilter(notCloudServiceEditionFilter())
          .addOpenApiCustomizer(globalUserSecurityCustomizer())
          .build();
    } else {
      // Cloud service edition
      openApi = GroupedOpenApi.builder()
          .displayName("/api (CloudService Edition User Api Document)")
          .group("user")
          .pathsToMatch("/api/v1/**")
          // Exclude privatized edition apis
          .addOpenApiMethodFilter(notPrivateServiceEditionFilter())
          .addOpenApiCustomizer(globalUserSecurityCustomizer())
          .build();
    }
    return openApi;
  }

  @Bean
  @Conditional(CloudServiceEditionCondition.class) // Private version does not display inner API docs
  public GroupedOpenApi doorApi() {
    return GroupedOpenApi.builder()
        .displayName("/innerapi (Inner System Api Document)")
        .group("Inner")
        .pathsToMatch("/innerapi/v1/**")
        .addOpenApiCustomizer(globalSysSecurityCustomizer())
        .build();
  }

  @Bean
  public GroupedOpenApi publicApi(ApplicationInfo applicationInfo) {
    GroupedOpenApi openApi;
    if (applicationInfo.isPrivateEdition()) {
      // Private edition
      openApi = GroupedOpenApi.builder()
          .displayName("/pubapi (Private Edition Public Api Document)")
          .group("public")
          .pathsToMatch("/pubapi/v1/**")
          // Exclude cloud service edition apis
          .addOpenApiMethodFilter(notCloudServiceEditionFilter())
          .build();
    } else {
      // Cloud service edition
      openApi = GroupedOpenApi.builder()
          .displayName("/pubapi (CloudService Edition Public Api Document)")
          .group("public")
          .pathsToMatch("/pubapi/v1/**")
          // Exclude privatized edition apis
          .addOpenApiMethodFilter(notPrivateServiceEditionFilter())
          .build();
    }
    return openApi;
  }

  private OpenApiMethodFilter notPrivateServiceEditionFilter() {
    return handlerMethod -> {
      // Check class level annotation
      boolean hasClassAnnotation = handlerMethod.getDeclaringClass()
          .isAnnotationPresent(PrivateEdition.class);
      // Check method level annotation
      boolean hasMethodAnnotation = handlerMethod.isAnnotationPresent(PrivateEdition.class);

      return !hasClassAnnotation && !hasMethodAnnotation;
    };
  }

  private OpenApiMethodFilter notCloudServiceEditionFilter() {
    return handlerMethod -> {
      // Check class level annotation
      boolean hasClassAnnotation = handlerMethod.getDeclaringClass()
          .isAnnotationPresent(CloudServiceEdition.class);
      // Check method level annotation
      boolean hasMethodAnnotation = handlerMethod.isAnnotationPresent(CloudServiceEdition.class);

      return !hasClassAnnotation && !hasMethodAnnotation;
    };
  }

  private OpenApiCustomizer globalUserSecurityCustomizer() {
    return openApi -> openApi
        // Use existing opaque tokens for authentication
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_USER_HTTP_NAME))
        // Use OAuth2 opaque tokens for authentication
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_USER_OAUTH2_NAME))
        .getComponents().addSecuritySchemes(SECURITY_SCHEME_USER_HTTP_NAME,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("opaque")
                .description("Use existing opaque tokens for authentication")
        ).addSecuritySchemes(SECURITY_SCHEME_USER_OAUTH2_NAME,
            new SecurityScheme()
                .type(Type.OAUTH2)
                .scheme("bearer")
                .bearerFormat("opaque")
                .flows(new OAuthFlows()
                    .password(new OAuthFlow()
                        .tokenUrl(oauth2TokenUrl)
                        .scopes(new Scopes()
                            .addString("read", "Read Permission")
                            .addString("write", "Write Permission"))))
                .description("Use OAuth2 opaque tokens for authentication")
        );
  }

  private OpenApiCustomizer globalSysSecurityCustomizer() {
    return openApi -> openApi
        // Use existing opaque tokens for authentication
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_SYS_HTTP_NAME))
        // Use OAuth2 opaque tokens for authentication
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_SYS_OAUTH2_NAME))
        .getComponents().addSecuritySchemes(SECURITY_SCHEME_SYS_HTTP_NAME,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("opaque")
                .description("Use existing opaque tokens for authentication")
        ).addSecuritySchemes(SECURITY_SCHEME_SYS_OAUTH2_NAME,
            new SecurityScheme()
                .type(Type.OAUTH2)
                .scheme("bearer")
                .bearerFormat("opaque")
                .flows(new OAuthFlows()
                    .clientCredentials(new OAuthFlow()
                        .tokenUrl(oauth2TokenUrl)
                        .scopes(new Scopes()
                            .addString("read", "Read Permission")
                            .addString("write", "Write Permission"))))
                .description("Use OAuth2 opaque tokens for authentication")
        );
  }
}
