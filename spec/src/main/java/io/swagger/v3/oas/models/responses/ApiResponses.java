package io.swagger.v3.oas.models.responses;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ApiResponses
 *
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#responsesObject"
 * @see "https://github.com/OAI/OpenAPI-Specification/blob/3.1.0/versions/3.1.0.md#responsesObject"
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponses extends LinkedHashMap<String, ApiResponse> {

  public static final String DEFAULT = "default";

  private Map<String, Object> extensions = null;

  public ApiResponses addApiResponse(String name, ApiResponse item) {
    this.put(name, item);
    return this;
  }

  /**
   * returns the default property from a ApiResponses instance.
   *
   * @return ApiResponse _default
   **/
  public ApiResponse getDefault() {
    return this.get(DEFAULT);
  }

  public void setDefault(ApiResponse _default) {
    addApiResponse(DEFAULT, _default);
  }

  public ApiResponses _default(ApiResponse _default) {
    setDefault(_default);
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

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

  @OpenAPI31
  public void addExtension31(String name, Object value) {
    if (name != null && (name.startsWith("x-oas-") || name.startsWith("x-oai-"))) {
      return;
    }
    addExtension(name, value);
  }

  public void setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  public ApiResponses extensions(Map<String, Object> extensions) {
    this.extensions = extensions;
    return this;
  }

  public static ApiResponses default_() {
    ApiResponses responses = new ApiResponses();
    ApiResponse response = new ApiResponse();
    response.description("default");
    responses.put(DEFAULT, response);
    return responses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ApiResponses apiResponses = (ApiResponses) o;
    return Objects.equals(this.extensions, apiResponses.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), extensions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiResponses {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    extensions: ").append(toIndentedString(extensions)).append("\n");
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

