package cloud.xcan.angus.swagger;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_SYS_HTTP_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SECURITY_SCHEME_USER_HTTP_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.spec.annotations.CloudServiceEdition;
import cloud.xcan.angus.spec.annotations.PrivateEdition;
import cloud.xcan.angus.spec.experimental.Assert;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @see OpenApiWebMvcResource#openapiJson(HttpServletRequest, String, Locale)
 * @see MultipleOpenApiWebMvcResource#openapiJson(HttpServletRequest, String, String, Locale)
 */
@Configuration
@ConditionalOnProperty(name = {"springdoc.api-docs.enabled"}, matchIfMissing = true)
//@EnableConfigurationProperties({SpringDocConfigProperties.class})
public class OpenApiAutoConfigurer {

  private static final List<String> ALLOWED_STATUS_CODES
      = Arrays.asList("200", "201", "204", "404");

  /**
   * Resolve jakarta.servlet.ServletException: Could not resolve view with name
   * 'forward:/swagger-ui/index.html' in servlet with name 'dispatcherServlet'.
   */
  @Bean
  public InternalResourceViewResolver defaultViewResolver() {
    return new InternalResourceViewResolver();
  }

  @Bean
  public FiltersOperationCustomizer filtersOperationCustomizer() {
    return new FiltersOperationCustomizer();
  }

  @Bean
  public OpenAPI openAPI(SpringDocConfigProperties doc, ApplicationInfo applicationInfo) {
    OpenAPI openAPI = doc.getOpenApi();
    Assert.assertNotNull(openAPI, "OpenAPI config should not be null");

    // OpenAPIService 会把空 Components 写回该共享 bean；若 schemas 为 null，
    // springdoc 2.8.6 SpecPropertiesCustomizer 在合并时会 NPE（#2960）。
    ensureComponentsSchemasMap(openAPI);

    if (!applicationInfo.isProdProfile()) {
      addSelfHostServer(applicationInfo, openAPI);
    }
    return openAPI;
  }

  private static void ensureComponentsSchemasMap(OpenAPI openAPI) {
    Components components = openAPI.getComponents();
    if (components == null) {
      components = new Components();
      openAPI.setComponents(components);
    }
    if (components.getSchemas() == null) {
      components.setSchemas(new LinkedHashMap<>());
    }
  }

  @Bean
  public GroupedOpenApi userApi(ApplicationInfo applicationInfo,
      FiltersOperationCustomizer filtersOperationCustomizer) {
    GroupedOpenApi openApi;
    if (applicationInfo.isPrivateEdition()) {
      // Private edition
      openApi = GroupedOpenApi.builder()
          .displayName("/api (Private Edition User Api Document)")
          .group("user")
          .pathsToMatch("/api/v1/**")
          // Exclude cloud service edition apis
          .addOpenApiMethodFilter(notCloudServiceEditionFilter())
          .addOpenApiCustomizer(addGlobalUserSecurityCustomizer())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer)
          .build();
    } else {
      // Cloud service edition
      openApi = GroupedOpenApi.builder()
          .displayName("/api (CloudService Edition User Api Document)")
          .group("user")
          .pathsToMatch("/api/v1/**")
          // Exclude privatized edition apis
          .addOpenApiMethodFilter(notPrivateServiceEditionFilter())
          .addOpenApiCustomizer(addGlobalUserSecurityCustomizer())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer)
          .build();
    }
    return openApi;
  }

  @Bean
  @Conditional(CloudServiceEditionCondition.class)
  // Private version does not display inner API docs
  public GroupedOpenApi innerApi(FiltersOperationCustomizer filtersOperationCustomizer) {
    return GroupedOpenApi.builder()
        .displayName("/innerapi (Inner System Api Document)")
        .group("inner")
        .pathsToMatch("/innerapi/v1/**")
        .addOpenApiCustomizer(addGlobalSysSecurityCustomizer())
        .addOpenApiCustomizer(removeDefaultResponses())
        .addOpenApiCustomizer(sortTagsAlphabetically())
        .addOperationCustomizer(filtersOperationCustomizer)
        .build();
  }

  @Bean
  @Conditional(CloudServiceEditionCondition.class)
  // Private version does not display inner API docs
  public GroupedOpenApi open2pApi(FiltersOperationCustomizer filtersOperationCustomizer) {
    return GroupedOpenApi.builder()
        .displayName("/openapi2p (Inner System Api Document)")
        .group("openapi2p")
        .pathsToMatch("/openapi2p/v1/**")
        .addOpenApiCustomizer(addGlobalSysSecurityCustomizer())
        .addOpenApiCustomizer(removeDefaultResponses())
        .addOpenApiCustomizer(sortTagsAlphabetically())
        .addOperationCustomizer(filtersOperationCustomizer)
        .build();
  }

  @Bean
  public GroupedOpenApi publicApi(ApplicationInfo applicationInfo,
      FiltersOperationCustomizer filtersOperationCustomizer) {
    GroupedOpenApi openApi;
    if (applicationInfo.isPrivateEdition()) {
      // Private edition
      openApi = GroupedOpenApi.builder()
          .displayName("/pubapi (Private Edition Public Api Document)")
          .group("public")
          .pathsToMatch("/pubapi/v1/**")
          // Exclude cloud service edition apis
          .addOpenApiMethodFilter(notCloudServiceEditionFilter())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer)
          .build();
    } else {
      // Cloud service edition
      openApi = GroupedOpenApi.builder()
          .displayName("/pubapi (CloudService Edition Public Api Document)")
          .group("public")
          .pathsToMatch("/pubapi/v1/**")
          // Exclude privatized edition apis
          .addOpenApiMethodFilter(notPrivateServiceEditionFilter())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer)
          .build();
    }
    return openApi;
  }

  @Bean
  public GroupedOpenApi rawApi(ApplicationInfo applicationInfo,
      FiltersOperationCustomizer filtersOperationCustomizer,
      ObjectProvider<RawApiDocCustomizer> rawApiDocCustomizer) {
    GroupedOpenApi.Builder builder;
    if (applicationInfo.isPrivateEdition()) {
      // Private edition
      builder = GroupedOpenApi.builder()
          .displayName("/** (Private Edition Raw Api Document)")
          .group("raw")
          .pathsToMatch("/**")
          .pathsToExclude("/api/v1/**", "/innerapi/v1/**", "/openapi2p/v1/**", "/pubapi/v1/**")
          // Exclude cloud service edition apis
          .addOpenApiMethodFilter(notCloudServiceEditionFilter())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer);
    } else {
      // Cloud service edition
      builder = GroupedOpenApi.builder()
          .displayName("/** (CloudService Edition Raw Api Document)")
          .group("raw")
          .pathsToMatch("/**")
          .pathsToExclude("/api/v1/**", "/innerapi/v1/**", "/openapi2p/v1/**", "/pubapi/v1/**")
          // Exclude privatized edition apis
          .addOpenApiMethodFilter(notPrivateServiceEditionFilter())
          .addOpenApiCustomizer(removeDefaultResponses())
          .addOpenApiCustomizer(sortTagsAlphabetically())
          .addOperationCustomizer(filtersOperationCustomizer);
    }
    // Optional extension point: the consuming application may provide a
    // RawApiDocCustomizer bean to give the raw group its own independent
    // OpenAPI Info/description (e.g. an artifact protocol API document).
    rawApiDocCustomizer.ifAvailable(builder::addOpenApiCustomizer);
    return builder.build();
  }

  /**
   * Optional extension point for customizing the <b>raw</b> group's OpenAPI document independently
   * (its own {@code Info}, description, external docs, etc.).
   *
   * <p>The raw group aggregates every path that is not under {@code /api}, {@code /innerapi},
   * {@code /openapi2p} or {@code /pubapi}. A consuming application can declare a single bean of this
   * type to override the shared global {@code Info} for this group only, without affecting the other
   * groups. When no such bean exists, the raw group keeps the shared global document unchanged.</p>
   */
  public interface RawApiDocCustomizer extends OpenApiCustomizer {

  }


  private void addSelfHostServer(ApplicationInfo applicationInfo, OpenAPI openAPI) {
    String url = String.format("http://%s", applicationInfo.getInstanceId());
    if (isNull(openAPI.getServers())
        || openAPI.getServers().stream().noneMatch(x -> url.equals(x.getUrl()))) {
      Server selfHost = new Server();
      selfHost.setUrl(url);
      openAPI.addServersItem(selfHost);
    }
  }

  private OpenApiCustomizer addGlobalUserSecurityCustomizer() {
    return openApi -> {
      ensureComponentsSchemasMap(openApi);
      openApi
          // Use existing opaque tokens for authentication
          .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_USER_HTTP_NAME))
          .getComponents()
          .addSecuritySchemes(SECURITY_SCHEME_USER_HTTP_NAME,
              new SecurityScheme()
                  .type(SecurityScheme.Type.HTTP)
                  .scheme("bearer")
                  .bearerFormat("opaque")
                  .description("Use existing opaque tokens for authentication")
          );
    };
  }

  private OpenApiCustomizer addGlobalSysSecurityCustomizer() {
    return openApi -> {
      ensureComponentsSchemasMap(openApi);
      openApi
          // Use existing opaque tokens for authentication
          .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_SYS_HTTP_NAME))
          .getComponents()
          .addSecuritySchemes(SECURITY_SCHEME_SYS_HTTP_NAME,
              new SecurityScheme()
                  .type(SecurityScheme.Type.HTTP)
                  .scheme("bearer")
                  .bearerFormat("opaque")
                  .description("Use existing opaque tokens for authentication")
          );
    };
  }

  private OpenApiCustomizer removeDefaultResponses() {
    return openApi -> openApi.getPaths().values().forEach(pathItem -> {
      pathItem.readOperations().forEach(this::filterResponses);
    });
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

  private void filterResponses(Operation operation) {
    if (operation.getResponses() != null) {
      operation.getResponses().keySet().removeIf(key -> !ALLOWED_STATUS_CODES.contains(key));
    }
  }

  private OpenApiCustomizer sortTagsAlphabetically() {
    return openApi -> {
      List<Tag> tags = openApi.getTags();
      if (tags != null) {
        List<Tag> sortedTags = tags.stream()
            .sorted(Comparator.comparing(Tag::getName))
            .collect(Collectors.toList());
        openApi.setTags(sortedTags);
      }
    };
  }
}
