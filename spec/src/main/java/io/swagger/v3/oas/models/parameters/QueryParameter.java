package io.swagger.v3.oas.models.parameters;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

/**
 * QueryParameter
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryParameter extends Parameter {

  private String in = "query";

  /**
   * returns the in property from a QueryParameter instance.
   *
   * @return String in
   **/
  @Override
  public String getIn() {
    return in;
  }

  @Override
  public void setIn(String in) {
    this.in = in;
  }

  @Override
  public QueryParameter in(String in) {
    this.in = in;
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
    QueryParameter queryParameter = (QueryParameter) o;
    return Objects.equals(this.in, queryParameter.in) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryParameter {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    in: ").append(toIndentedString(in)).append("\n");
    sb.append("}");
    return sb.toString();
  }

}

