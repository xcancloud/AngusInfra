package io.swagger.v3.oas.models.media;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ComposedSchema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComposedSchema extends Schema<Object> {


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComposedSchema {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("}");
    return sb.toString();
  }
}
