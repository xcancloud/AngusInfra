package io.swagger.v3.oas.models;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_PARAM_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_PATH_NUM;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_TAG_NUM;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OpenAPI
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAPI {

  @NotEmpty
  @io.swagger.v3.oas.annotations.media.Schema(description =
      "This string MUST be the semantic version number of the OpenAPI Specification version that the OpenAPI document uses. "
          + "The openapi field SHOULD be used by tooling specifications and clients to interpret the OpenAPI document. This is not related to the API info.version string.",
      requiredMode = RequiredMode.REQUIRED, maxLength = DEFAULT_KEY_LENGTH)
  private String openapi = "3.0.1";

  @NotNull
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Provides metadata about the API. The metadata MAY be used by tooling as required.",
      requiredMode = RequiredMode.REQUIRED)
  private Info info = null;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Additional external documentation.")
  private ExternalDocumentation externalDocs = null;

  @Size(max = DEFAULT_PARAM_SIZE)
  @io.swagger.v3.oas.annotations.media.Schema(
      description =
          "An array of Server Objects, which provide connectivity information to a target server. "
              + "If the servers property is not provided, or is an empty array, the default value would be a Server Object with a url value of /.")
  private List<Server> servers = null;

  @Size(max = DEFAULT_PARAM_SIZE)
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "A declaration of which security mechanisms can be used across the API. "
          + "The list of values includes alternative security requirement objects that can be used. "
          + "Only one of the security requirement objects need to be satisfied to authorize a request. "
          + "Individual operations can override this definition. To make security optional, "
          + "an empty security requirement ({}) can be included in the array.")
  private List<SecurityRequirement> security = null;

  @Size(max = MAX_OPENAPI_TAG_NUM)
  @io.swagger.v3.oas.annotations.media.Schema(
      description =
          "A list of tags used by the specification with additional metadata. The order of the tags can be used to reflect on their order by the parsing tools. "
              + "Not all tags that are used by the Operation Object must be declared. The tags that are not declared MAY be organized randomly or based on the tools' logic. "
              + "Each tag name in the list MUST be unique.")
  private List<Tag> tags = null;

  @Size(max = MAX_OPENAPI_PATH_NUM)
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "The available paths and operations for the API.")
  private Paths paths = null;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "An element to hold various schemas for the specification.")
  private Components components = null;

  @Size(max = DEFAULT_PARAM_SIZE)
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Specification Extensions.")
  private Map<String, Object> extensions = null;

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  private String jsonSchemaDialect;

  public OpenAPI() {
  }

  public OpenAPI(SpecVersion specVersion) {
    this.specVersion = specVersion;
  }

  private SpecVersion specVersion = SpecVersion.V30;

  @JsonIgnore
  public SpecVersion getSpecVersion() {
    return this.specVersion;
  }

  public void setSpecVersion(SpecVersion specVersion) {
    this.specVersion = specVersion;
  }

  public OpenAPI specVersion(SpecVersion specVersion) {
    this.setSpecVersion(specVersion);
    return this;
  }

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  private Map<String, PathItem> webhooks = null;

  /**
   * returns the openapi property from a OpenAPI instance.
   *
   * @return String openapi
   **/

  public String getOpenapi() {
    return openapi;
  }

  public void setOpenapi(String openapi) {
    this.openapi = openapi;
  }

  public OpenAPI openapi(String openapi) {
    this.openapi = openapi;
    return this;
  }

  /**
   * returns the info property from a OpenAPI instance.
   *
   * @return Info info
   **/

  public Info getInfo() {
    return info;
  }

  public void setInfo(Info info) {
    this.info = info;
  }

  public OpenAPI info(Info info) {
    this.info = info;
    return this;
  }

  /**
   * returns the externalDocs property from a OpenAPI instance.
   *
   * @return ExternalDocumentation externalDocs
   **/

  public ExternalDocumentation getExternalDocs() {
    return externalDocs;
  }

  public void setExternalDocs(ExternalDocumentation externalDocs) {
    this.externalDocs = externalDocs;
  }

  public OpenAPI externalDocs(ExternalDocumentation externalDocs) {
    this.externalDocs = externalDocs;
    return this;
  }

  /**
   * Servers defined in the API
   *
   * @return List&lt;Server&gt; servers
   **/
  @ThirdExtension
  public List<Server> getServers() {
    return isNotEmpty(servers) ? servers.stream().filter(Server::isNotEmptyContent)
        .collect(Collectors.toList()) : null;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }

  public OpenAPI servers(List<Server> servers) {
    this.servers = servers;
    return this;
  }

  public OpenAPI addServersItem(Server serversItem) {
    if (this.servers == null) {
      this.servers = new ArrayList<>();
    }
    this.servers.add(serversItem);
    return this;
  }

  /**
   * returns the security property from a OpenAPI instance.
   *
   * @return List&lt;SecurityRequirement&gt; security
   **/

  public List<SecurityRequirement> getSecurity() {
    return security;
  }

  public void setSecurity(List<SecurityRequirement> security) {
    this.security = security;
  }

  public OpenAPI security(List<SecurityRequirement> security) {
    this.security = security;
    return this;
  }

  public OpenAPI addSecurityItem(SecurityRequirement securityItem) {
    if (this.security == null) {
      this.security = new ArrayList<>();
    }
    this.security.add(securityItem);
    return this;
  }

  /**
   * returns the tags property from a OpenAPI instance.
   *
   * @return List&lt;Tag&gt; tags
   **/

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  public OpenAPI tags(List<Tag> tags) {
    this.tags = tags;
    return this;
  }

  public OpenAPI addTagsItem(Tag tagsItem) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }
    this.tags.add(tagsItem);
    return this;
  }

  /**
   * returns the paths property from a OpenAPI instance.
   *
   * @return Paths paths
   **/

  public Paths getPaths() {
    return paths;
  }

  public void setPaths(Paths paths) {
    this.paths = paths;
  }

  public OpenAPI paths(Paths paths) {
    this.paths = paths;
    return this;
  }

  /**
   * returns the components property from a OpenAPI instance.
   *
   * @return Components components
   **/

  public Components getComponents() {
    return components;
  }

  public void setComponents(Components components) {
    this.components = components;
  }

  public OpenAPI components(Components components) {
    this.components = components;
    return this;
  }

  /*
   * helpers
   */

  public OpenAPI path(String name, PathItem path) {
    if (this.paths == null) {
      this.paths = new Paths();
    }

    this.paths.addPathItem(name, path);
    return this;
  }

  public OpenAPI schema(String name, Schema schema) {
    if (components == null) {
      this.components = new Components();
    }
    components.addSchemas(name, schema);
    return this;
  }

  public OpenAPI schemaRequirement(String name, SecurityScheme securityScheme) {
    if (components == null) {
      this.components = new Components();
    }
    components.addSecuritySchemes(name, securityScheme);
    return this;
  }

  /**
   * returns the webhooks property from a OpenAPI instance.
   *
   * @return Map&lt;String, PathItem&gt; webhooks
   * @since 2.2.0 (OpenAPI 3.1.0)
   **/

  @OpenAPI31
  public Map<String, PathItem> getWebhooks() {
    return webhooks;
  }

  @OpenAPI31
  public void setWebhooks(Map<String, PathItem> webhooks) {
    this.webhooks = webhooks;
  }

  @OpenAPI31
  public OpenAPI webhooks(Map<String, PathItem> webhooks) {
    this.webhooks = webhooks;
    return this;
  }

  @OpenAPI31
  public OpenAPI addWebhooks(String key, PathItem pathItem) {
    if (this.webhooks == null) {
      this.webhooks = new LinkedHashMap<>();
    }
    this.webhooks.put(key, pathItem);
    return this;
  }

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  public String getJsonSchemaDialect() {
    return jsonSchemaDialect;
  }

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  public void setJsonSchemaDialect(String jsonSchemaDialect) {
    this.jsonSchemaDialect = jsonSchemaDialect;
  }

  @OpenAPI31
  public OpenAPI jsonSchemaDialect(String jsonSchemaDialect) {
    this.jsonSchemaDialect = jsonSchemaDialect;
    return this;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenAPI openAPI = (OpenAPI) o;
    return Objects.equals(this.openapi, openAPI.openapi) &&
        Objects.equals(this.info, openAPI.info) &&
        Objects.equals(this.externalDocs, openAPI.externalDocs) &&
        Objects.equals(this.servers, openAPI.servers) &&
        Objects.equals(this.security, openAPI.security) &&
        Objects.equals(this.tags, openAPI.tags) &&
        Objects.equals(this.paths, openAPI.paths) &&
        Objects.equals(this.components, openAPI.components) &&
        Objects.equals(this.webhooks, openAPI.webhooks) &&
        Objects.equals(this.extensions, openAPI.extensions) &&
        Objects.equals(this.jsonSchemaDialect, openAPI.jsonSchemaDialect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(openapi, info, externalDocs, servers, security, tags, paths, components,
        webhooks, extensions, jsonSchemaDialect);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  @JsonAnySetter
  public void addExtension(String name, Object value) {
    if (name == null || name.isEmpty() || !name.startsWith("x-")) {
      return;
    }
    if (this.extensions == null) {
      this.extensions = new LinkedHashMap<>();
    }
    this.extensions.put(name, value);
  }

  @OpenAPI31
  public void addExtension31(String name, Object value) {
    if (name != null && (name.startsWith("x-oas-") || name.startsWith("x-oai-"))) {
      return;
    }
    addExtension(name, value);
  }

  public void setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  public OpenAPI extensions(Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenAPI {\n");

    sb.append("    openapi: ").append(toIndentedString(openapi)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
    sb.append("    externalDocs: ").append(toIndentedString(externalDocs)).append("\n");
    sb.append("    servers: ").append(toIndentedString(servers)).append("\n");
    sb.append("    security: ").append(toIndentedString(security)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    paths: ").append(toIndentedString(paths)).append("\n");
    sb.append("    components: ").append(toIndentedString(components)).append("\n");
    if (specVersion == SpecVersion.V31) {
      sb.append("    webhooks: ").append(toIndentedString(webhooks)).append("\n");
    }
    if (specVersion == SpecVersion.V31) {
      sb.append("    jsonSchemaDialect: ").append(toIndentedString(jsonSchemaDialect))
          .append("\n");
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

