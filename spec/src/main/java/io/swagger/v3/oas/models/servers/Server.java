package io.swagger.v3.oas.models.servers;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * Server
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#serverObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#serverObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Server {

  @NotBlank
  @Length(max = DEFAULT_URL_LENGTH_X2)
  @Schema(description = "A URL to the target host. Note: `This MUST be in the form of a URL`.",
      example = "https://{env}-api.xcan.cloud:{port}/{basePath}", requiredMode = RequiredMode.REQUIRED)
  private String url = null;

  @Length(max = DEFAULT_REMARK_LENGTH_X4)
  @Schema(description = "An optional string describing the host designated by the URL. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.",
      example = "The production API server")
  private String description = null;

  @Schema(description =
      "A map between a variable name and its value. The value is used for substitution in the server's URL template. For example: \n"
          + "<p>"
          + "\"variables\": {\n"
          + "        \"env\": {\n"
          + "          \"default\": \"prod\",\n"
          + "          \"description\": \"This is a production environment\"\n"
          + "        },\n"
          + "        \"port\": {\n"
          + "          \"enum\": [\n"
          + "            \"8443\",\n"
          + "            \"443\"\n"
          + "          ],\n"
          + "          \"default\": \"8443\"\n"
          + "        },\n"
          + "        \"basePath\": {\n"
          + "          \"default\": \"v2\"\n"
          + "        }\n"
          + "      }"
          + "</p>")
  private ServerVariables variables = null;

  @Schema(description = "The extensions of the OpenAPI server schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  @JsonIgnore
  @ThirdExtension
  public boolean isValidUrl() {
    return isNotEmpty(url) && !"/".equals(url);
  }

  @ThirdExtension
  public boolean isEmptyContent() {
    return (isEmpty(url) || "/".equals(url)) && isEmpty(description)
        && isEmpty(variables) && isEmpty(extensions);
  }

  @ThirdExtension
  public boolean isNotEmptyContent() {
    return !isEmptyContent();
  }

  /**
   * returns the url property from a Server instance.
   *
   * @return String url
   **/
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Server url(String url) {
    this.url = url;
    return this;
  }

  /**
   * returns the description property from a Server instance.
   *
   * @return String description
   **/

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Server description(String description) {
    this.description = description;
    return this;
  }

  /**
   * returns the variables property from a Server instance.
   *
   * @return ServerVariables variables
   **/

  public ServerVariables getVariables() {
    return variables;
  }

  public void setVariables(ServerVariables variables) {
    this.variables = variables;
  }

  public Server variables(ServerVariables variables) {
    this.variables = variables;
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
    Server server = (Server) o;
    return Objects.equals(this.url, server.url) &&
        Objects.equals(this.description, server.description) &&
        Objects.equals(this.variables, server.variables) &&
        Objects.equals(this.extensions, server.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, description, variables, extensions);
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

  public Server extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Server {\n");

    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
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

