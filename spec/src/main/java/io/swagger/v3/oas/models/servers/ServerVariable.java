package io.swagger.v3.oas.models.servers;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

/**
 * ServerVariable
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#serverVariableObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#serverVariableObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerVariable {

  //@NotEmpty
  @Schema(description = "An enumeration of string values to be used if the substitution options are from a limited set. Note: The array MUST NOT be empty.",
      type = "array", example = "[\"prod\", \"dev\", \"beta\"]")
  private List<String> _enum = null;

  //@NotEmpty
  @Length(max = MAX_PARAM_NAME_LENGTH)
  @Schema(description =
      "The default value to use for substitution, which SHALL be sent if an alternate value is not supplied. Note: This behavior is different than the Schema Object's treatment of default values, because in those cases parameter values are optional. "
          + "If the enum is defined, the value MUST exist in the enum's values.", example = "prod")
  private String _default = null;

  @Length(max = DEFAULT_REMARK_LENGTH_X4)
  @Schema(description = "An optional description for the server variable. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.", example = "This is a production environment")
  private String description = null;

  @Schema(description = "The extensions of the OpenAPI server variable schema. For more information, please see: [Specification Extensions](https://swagger.io/specification/#info-object)")
  private java.util.Map<String, Object> extensions = null;

  /**
   * returns the _enum property from a ServerVariable instance.
   *
   * @return List&lt;String&gt; _enum
   **/

  public List<String> getEnum() {
    return _enum;
  }

  public void setEnum(List<String> _enum) {
    this._enum = _enum;
  }

  public ServerVariable _enum(List<String> _enum) {
    this._enum = _enum;
    return this;
  }

  public ServerVariable addEnumItem(String _enumItem) {
    if (this._enum == null) {
      this._enum = new ArrayList<>();
    }
    this._enum.add(_enumItem);
    return this;
  }

  /**
   * returns the _default property from a ServerVariable instance.
   *
   * @return String _default
   **/

  public String getDefault() {
    return _default;
  }

  public void setDefault(String _default) {
    this._default = _default;
  }

  public ServerVariable _default(String _default) {
    this._default = _default;
    return this;
  }

  /**
   * returns the description property from a ServerVariable instance.
   *
   * @return String description
   **/

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ServerVariable description(String description) {
    this.description = description;
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
    ServerVariable serverVariable = (ServerVariable) o;
    return Objects.equals(this._enum, serverVariable._enum) &&
        Objects.equals(this._default, serverVariable._default) &&
        Objects.equals(this.description, serverVariable.description) &&
        Objects.equals(this.extensions, serverVariable.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_enum, _default, description, extensions);
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

  public ServerVariable extensions(java.util.Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServerVariable {\n");

    sb.append("    _enum: ").append(toIndentedString(_enum)).append("\n");
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

