package io.swagger.v3.oas.models.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;

/**
 * ByteArraySchema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ByteArraySchema extends Schema<byte[]> {

  public ByteArraySchema() {
    super("string", "byte");
  }

  @Override
  public ByteArraySchema type(String type) {
    super.setType(type);
    return this;
  }

  @Override
  public ByteArraySchema format(String format) {
    super.setFormat(format);
    return this;
  }

  @Override
  public ByteArraySchema _default(byte[] _default) {
    super.setDefault(_default);
    return this;
  }

  @Override
  protected byte[] cast(Object value) {
    if (value != null) {
      try {
        if (value instanceof byte[]) {
          return (byte[]) value;
        } else {
          return value.toString().getBytes();
        }
      } catch (Exception e) {
      }
    }
    return null;
  }

  public ByteArraySchema _enum(List<byte[]> _enum) {
    super.setEnum(_enum);
    return this;
  }

  public ByteArraySchema addEnumItem(byte[] _enumItem) {
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
    sb.append("class ByteArraySchema {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("}");
    return sb.toString();
  }
}
