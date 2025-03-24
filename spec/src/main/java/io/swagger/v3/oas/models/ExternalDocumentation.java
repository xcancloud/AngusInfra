package io.swagger.v3.oas.models;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * ExternalDocumentation
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#externalDocumentationObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#externalDocumentationObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalDocumentation {

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  @Schema(description = "A description of the target documentation. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.",
      example = "Find more info here")
  private String description;

  @NotBlank
  @Length(max = DEFAULT_URL_LENGTH_X2)
  @Schema(description = "A description of the target documentation. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.",
      example = "https://example.com", requiredMode = RequiredMode.REQUIRED)
  private String url;

  @Schema(description = "The extensions of the OpenAPI external documents. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  /**
   * returns the description property from a ExternalDocumentation instance.
   *
   * @return String description
   **/

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ExternalDocumentation description(String description) {
    this.description = description;
    return this;
  }

  /**
   * returns the url property from a ExternalDocumentation instance.
   *
   * @return String url
   **/

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public ExternalDocumentation url(String url) {
    this.url = url;
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
    ExternalDocumentation externalDocumentation = (ExternalDocumentation) o;
    return Objects.equals(this.description, externalDocumentation.description) &&
        Objects.equals(this.url, externalDocumentation.url) &&
        Objects.equals(this.extensions, externalDocumentation.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, url, extensions);
  }

  public java.util.Map<String, Object> getExtensions() {
    return extensions;
  }

  @JsonAnySetter
  public void addExtension(String name, Object value) {
    if (name == null || name.isEmpty() || !name.startsWith("x-")) {
      return;
    }
    if (this.extensions == null) {
      this.extensions = new java.util.LinkedHashMap<>();
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

  public void setExtensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  public ExternalDocumentation extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalDocumentation {\n");

    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

