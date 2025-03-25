package io.swagger.v3.oas.models.info;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#infoObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#infoObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Info {

  @NotEmpty
  @Length(max = MAX_OPENAPI_NAME_LENGTH)
  @Schema(description = "The title of the OpenAPI document.",
      requiredMode = RequiredMode.REQUIRED, maxLength = MAX_OPENAPI_NAME_LENGTH)
  private String title = null;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  @Schema(description = "A description of the API. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.",
      maxLength = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description = null;

  @Length(max = DEFAULT_URL_LENGTH_X2)
  @Schema(description = "A URL to the Terms of Service for the API. Note: `This MUST be in the form of a URL`.",
      maxLength = DEFAULT_URL_LENGTH_X2)
  private String termsOfService = null;

  @Valid
  @Schema(description = "The contact information for the exposed API.")
  private Contact contact = null;

  @Valid
  @Schema(description = "The license information for the exposed API.")
  private License license = null;

  @NotEmpty
  @Length(max = DEFAULT_KEY_LENGTH)
  @Schema(description = "The version of the OpenAPI document. Note: `Which is distinct from the [OpenAPI Specification version](https://swagger.io/specification/#oas-version) or the API implementation version`.",
      requiredMode = RequiredMode.REQUIRED)
  private String version = null;

  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "The extensions of the OpenAPI info schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @io.swagger.v3.oas.models.annotations.OpenAPI31
  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "A short summary of the API.", maxLength = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary = null;

  /**
   * returns the title property from a Info instance.
   *
   * @return String title
   **/

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Info title(String title) {
    this.title = title;
    return this;
  }

  /**
   * returns the description property from a Info instance.
   *
   * @return String description
   **/

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Info description(String description) {
    this.description = description;
    return this;
  }

  /**
   * returns the termsOfService property from a Info instance.
   *
   * @return String termsOfService
   **/

  public String getTermsOfService() {
    return termsOfService;
  }

  public void setTermsOfService(String termsOfService) {
    this.termsOfService = termsOfService;
  }

  public Info termsOfService(String termsOfService) {
    this.termsOfService = termsOfService;
    return this;
  }

  /**
   * returns the contact property from a Info instance.
   *
   * @return Contact contact
   **/

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  public Info contact(Contact contact) {
    this.contact = contact;
    return this;
  }

  /**
   * returns the license property from a Info instance.
   *
   * @return License license
   **/

  public License getLicense() {
    return license;
  }

  public void setLicense(License license) {
    this.license = license;
  }

  public Info license(License license) {
    this.license = license;
    return this;
  }

  /**
   * returns the version property from a Info instance.
   *
   * @return String version
   **/

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Info version(String version) {
    this.version = version;
    return this;
  }

  /**
   * returns the summary property from a Info instance.
   *
   * @return String
   * @since 2.2.0 (OpenAPI 3.1.0)
   **/
  @OpenAPI31
  public String getSummary() {
    return summary;
  }

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * @since 2.2.0 (OpenAPI 3.1.0)
   */
  @OpenAPI31
  public Info summary(String summary) {
    this.summary = summary;
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
    Info info = (Info) o;
    return Objects.equals(this.title, info.title) &&
        Objects.equals(this.description, info.description) &&
        Objects.equals(this.termsOfService, info.termsOfService) &&
        Objects.equals(this.contact, info.contact) &&
        Objects.equals(this.license, info.license) &&
        Objects.equals(this.version, info.version) &&
        Objects.equals(this.extensions, info.extensions) &&
        Objects.equals(this.summary, info.summary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, termsOfService, contact, license, version, extensions,
        summary);
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

  public Info extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Info {\n");

    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    termsOfService: ").append(toIndentedString(termsOfService)).append("\n");
    sb.append("    contact: ").append(toIndentedString(contact)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

