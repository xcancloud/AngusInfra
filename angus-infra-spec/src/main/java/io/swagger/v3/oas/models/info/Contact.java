package io.swagger.v3.oas.models.info;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_EMAIL_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * Contact
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#contactObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#contactObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "The identifying name of the contact person/organization.", example = "API Support")
  private String name = null;

  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(description = "The URL pointing to the contact information. Note: `This MUST be in the form of a URL`.", example = "https://www.xcan.cloud/support")
  private String url = null;

  @Length(max = MAX_EMAIL_LENGTH)
  @Schema(description = "The email address of the contact person/organization. Note: `This MUST be in the form of an email address`.", example = "api_support@example.com")
  private String email = null;

  @Schema(description = "The extensions of the OpenAPI contact schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  /**
   * returns the name property from a Contact instance.
   *
   * @return String name
   **/

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Contact name(String name) {
    this.name = name;
    return this;
  }

  /**
   * returns the url property from a Contact instance.
   *
   * @return String url
   **/

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Contact url(String url) {
    this.url = url;
    return this;
  }

  /**
   * returns the email property from a Contact instance.
   *
   * @return String email
   **/

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Contact email(String email) {
    this.email = email;
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
    Contact contact = (Contact) o;
    return Objects.equals(this.name, contact.name) &&
        Objects.equals(this.url, contact.url) &&
        Objects.equals(this.email, contact.email) &&
        Objects.equals(this.extensions, contact.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, email, extensions);
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

  public Contact extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Contact {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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

