package io.swagger.v3.oas.models.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.models.SpecVersion;

/**
 * JsonSchema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonSchema extends Schema<Object> {

  public JsonSchema() {
    specVersion(SpecVersion.V31);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class JsonSchema {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("}");
    return sb.toString();
  }
}
