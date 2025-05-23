package io.swagger.v3.oas.models.responses;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Content;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ApiResponse
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#responseObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#responseObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

  private String description = null;
  private Map<String, Header> headers = null;
  private Content content = null;
  private Map<String, Link> links = null;
  private Map<String, Object> extensions = null;
  private String $ref = null;

  /**
   * returns the description property from a ApiResponse instance.
   *
   * @return String description
   **/

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ApiResponse description(String description) {
    this.description = description;
    return this;
  }

  /**
   * returns the headers property from a ApiResponse instance.
   *
   * @return headers
   **/

  public Map<String, Header> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, Header> headers) {
    this.headers = headers;
  }

  public ApiResponse headers(Map<String, Header> headers) {
    this.headers = headers;
    return this;
  }

  public ApiResponse addHeaderObject(String name, Header header) {
    if (this.headers == null) {
      headers = new LinkedHashMap<>();
    }
    headers.put(name, header);
    return this;
  }

  /**
   * returns the content property from a ApiResponse instance.
   *
   * @return Content content
   **/

  public Content getContent() {
    return content;
  }

  public void setContent(Content content) {
    this.content = content;
  }

  public ApiResponse content(Content content) {
    this.content = content;
    return this;
  }

  /**
   * returns the links property from a ApiResponse instance.
   *
   * @return Link links
   **/

  public Map<String, Link> getLinks() {
    return links;
  }

  public void setLinks(Map<String, Link> links) {
    this.links = links;
  }

  public ApiResponse links(Map<String, Link> links) {
    this.links = links;
    return this;
  }

  public ApiResponse addLink(String name, Link link) {
    if (this.links == null) {
      this.links = new LinkedHashMap<>();
    }
    this.links.put(name, link);
    return this;
  }

  public ApiResponse link(String name, Link link) {
    return this.addLink(name, link);
  }

  /**
   * returns the $ref property from an ApiResponse instance.
   *
   * @return String $ref
   **/
  public String get$ref() {
    return $ref;
  }

  public void set$ref(String $ref) {
    if ($ref != null && ($ref.indexOf('.') == -1 && $ref.indexOf('/') == -1)) {
      $ref = "#/components/responses/" + $ref;
    }
    this.$ref = $ref;
  }

  @ThirdExtension
  public static String format$ref(String $ref) {
    if ($ref != null && ($ref.indexOf('.') == -1 && $ref.indexOf('/') == -1)) {
      return Components.COMPONENTS_RESPONSES_REF + $ref;
    }
    return $ref;
  }

  public ApiResponse $ref(String $ref) {
    set$ref($ref);
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
    ApiResponse apiResponse = (ApiResponse) o;
    return Objects.equals(this.description, apiResponse.description) &&
        Objects.equals(this.headers, apiResponse.headers) &&
        Objects.equals(this.content, apiResponse.content) &&
        Objects.equals(this.links, apiResponse.links) &&
        Objects.equals(this.extensions, apiResponse.extensions) &&
        Objects.equals(this.$ref, apiResponse.$ref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, headers, content, links, extensions, $ref);
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

  public ApiResponse extensions(Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiResponse {\n");

    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    extensions: ").append(toIndentedString(extensions)).append("\n");
    sb.append("    $ref: ").append(toIndentedString($ref)).append("\n");
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

