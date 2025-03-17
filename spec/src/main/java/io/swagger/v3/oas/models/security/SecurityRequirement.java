package io.swagger.v3.oas.models.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * SecurityRequirement
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#securityRequirementObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#securityRequirementObject"
 */
@Schema(description = "Key is the name of security scheme, values is security requirement values. "
    + "If the security scheme is of type `oauth2` or `openIdConnect`, then the value is a list of scope names required for the execution, "
    + "and the list MAY be empty if authorization does not require a specified scope.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityRequirement extends LinkedHashMap<String, List<String>> {

  public SecurityRequirement() {
  }

  public SecurityRequirement addList(String name, String item) {
    this.put(name, Arrays.asList(item));
    return this;
  }

  public SecurityRequirement addList(String name, List<String> item) {
    this.put(name, item);
    return this;
  }

  public SecurityRequirement addList(String name) {
    this.put(name, new ArrayList<>());
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
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityRequirement {\n");
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

