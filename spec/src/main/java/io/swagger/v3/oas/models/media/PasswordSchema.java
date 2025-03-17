package io.swagger.v3.oas.models.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/**
 * PasswordSchema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordSchema extends Schema<String> {

  public PasswordSchema() {
    super("string", "password");
  }

  @Override
  public PasswordSchema type(String type) {
    super.setType(type);
    return this;
  }

  @Override
  public PasswordSchema format(String format) {
    super.setFormat(format);
    return this;
  }

  public PasswordSchema _default(String _default) {
    super.setDefault(_default);
    return this;
  }

  @Override
  protected String cast(Object value) {
    if (value != null) {
      try {
        return value.toString();
      } catch (Exception e) {
      }
    }
    return null;
  }

  public PasswordSchema addEnumItem(String _enumItem) {
    super.addEnumItemObject(_enumItem);
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
    sb.append("class PasswordSchema {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("}");
    return sb.toString();
  }
}
