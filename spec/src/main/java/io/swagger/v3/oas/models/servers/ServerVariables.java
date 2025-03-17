package io.swagger.v3.oas.models.servers;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ServerVariables
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#serverVariablesObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerVariables extends LinkedHashMap<String, ServerVariable> {

  public ServerVariables() {
  }

  private Map<String, Object> extensions = null;

  public ServerVariables addServerVariable(String name, ServerVariable item) {
    this.put(name, item);
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
    ServerVariables serverVariables = (ServerVariables) o;
    return Objects.equals(this.extensions, serverVariables.extensions) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(extensions, super.hashCode());
  }

  /**
   * @deprecated As extensions don't make sense at this level
   */
  @Deprecated
  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  /**
   * @deprecated As extensions don't make sense at this level
   */
  @Deprecated
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

  /**
   * @deprecated As extensions don't make sense at this level
   */
  @Deprecated
  public void setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  /**
   * @deprecated As extensions don't make sense at this level
   */
  @Deprecated
  public ServerVariables extensions(Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServerVariables {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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
