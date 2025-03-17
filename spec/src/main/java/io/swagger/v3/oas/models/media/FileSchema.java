package io.swagger.v3.oas.models.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/**
 * FileSchema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileSchema extends Schema<String> {

  public FileSchema() {
    super("string", "binary");
  }

  @Override
  public FileSchema type(String type) {
    super.setType(type);
    return this;
  }

  @Override
  public FileSchema format(String format) {
    super.setFormat(format);
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
    sb.append("class FileSchema {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("}");
    return sb.toString();
  }
}
