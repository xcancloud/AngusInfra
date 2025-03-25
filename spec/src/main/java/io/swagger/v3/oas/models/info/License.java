package io.swagger.v3.oas.models.info;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * License
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#licenseObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#licenseObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class License {

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @Schema(description = "The license name used for the API.", example = "Apache 2.0", requiredMode = RequiredMode.REQUIRED)
  private String name = null;

  @Length(max = DEFAULT_URL_LENGTH_X2)
  @Schema(description = "A URL to the license used for the API. Note: This MUST be in the form of a URL. The `url` field is mutually exclusive of the `identifier` field.",
      example = "https://www.apache.org/licenses/LICENSE-2.0")
  private String url = null;

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @io.swagger.v3.oas.models.annotations.OpenAPI31
  @Length(max = DEFAULT_CODE_LENGTH_X2)
  @Schema(description = "An SPDX license expression for the API. Note: The `identifier` field is mutually exclusive of the `url` field.",
      example = "Apache-2.0")
  private String identifier = null;

  @Schema(description = "The extensions of the OpenAPI license schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  /**
   * returns the name property from a License instance.
   *
   * @return String name
   **/

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public License name(String name) {
    this.name = name;
    return this;
  }

  /**
   * returns the url property from a License instance.
   *
   * @return String url
   **/

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public License url(String url) {
    this.url = url;
    return this;
  }

  /**
   * returns the identifier property from a License instance.
   *
   * @return String identifier
   * @since 2.2.0 (OpenAPI 3.1.0)
   **/
  @OpenAPI31
  public String getIdentifier() {
    return identifier;
  }

  @OpenAPI31
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @OpenAPI31
  public License identifier(String identifier) {
    this.identifier = identifier;
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
    License license = (License) o;
    return Objects.equals(this.name, license.name) &&
        Objects.equals(this.url, license.url) &&
        Objects.equals(this.identifier, license.identifier) &&
        Objects.equals(this.extensions, license.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, identifier, extensions);
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

  public License extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class License {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
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

