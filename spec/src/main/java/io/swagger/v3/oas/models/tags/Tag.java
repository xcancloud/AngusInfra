package io.swagger.v3.oas.models.tags;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * Tag
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#tagObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#tagObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag {

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @Schema(description = "The name of the tag. Note: Each tag name in the list MUST be unique.",
      example = "User", requiredMode = RequiredMode.REQUIRED)
  private String name = null;

  @Length(max = DEFAULT_REMARK_LENGTH_X4)
  @Schema(description = "A description for the tag. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.",
      example = "User Rest API")
  private String description = null;

  @Schema(description = "Additional external documentation for this tag. For more information, please see: [External Documentation Object](https://swagger.io/specification/#info-object)")
  private ExternalDocumentation externalDocs = null;

  @Schema(description = "The extensions of the OpenAPI tag schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private Map<String, Object> extensions = new HashMap<>();

  /**
   * returns the name property from a Tag instance.
   *
   * @return String name
   **/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Tag name(String name) {
    this.name = name;
    return this;
  }

  /**
   * returns the description property from a Tag instance.
   *
   * @return String description
   **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Tag description(String description) {
    this.description = description;
    return this;
  }

  /**
   * returns the externalDocs property from a Tag instance.
   *
   * @return ExternalDocumentation externalDocs
   **/
  public ExternalDocumentation getExternalDocs() {
    return externalDocs;
  }

  public void setExternalDocs(ExternalDocumentation externalDocs) {
    this.externalDocs = externalDocs;
  }

  public Tag externalDocs(ExternalDocumentation externalDocs) {
    this.externalDocs = externalDocs;
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
    Tag tag = (Tag) o;
    return Objects.equals(this.name, tag.name) &&
        Objects.equals(this.description, tag.description) &&
        Objects.equals(this.externalDocs, tag.externalDocs) &&
        Objects.equals(this.extensions, tag.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, externalDocs, extensions);
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

  public void setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  public Tag extensions(Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Tag {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    externalDocs: ").append(toIndentedString(externalDocs)).append("\n");
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
