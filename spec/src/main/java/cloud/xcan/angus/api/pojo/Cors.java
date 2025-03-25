package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.spec.http.HttpResponseHeader;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class Cors {

  /**
   * @see HttpResponseHeader#Access_Control_Allow_Origin
   */
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(description = "Specifying which web sites can participate in cross-origin resource sharing, default *", example = "*")
  private String allowCorsOrigin = "*";

  /**
   * @see HttpResponseHeader#Access_Control_Allow_Credentials
   */
  @Schema(description = "Specifying third-party sites may be able to carry out privileged actions and retrieve sensitive information, default true", example = "true")
  private Boolean allowCorsCredentials = true;

  /**
   * @see HttpResponseHeader#Access_Control_Allow_Headers
   */
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(description = "Specifying which HTTP request headers are allowed for the client to use, default empty")
  private String allowCorsRequestHeaders = null;

  /**
   * @see HttpResponseHeader#Access_Control_Allow_Methods
   */
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(description = "Specifying which HTTP methods are allowed, default GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD",
      example = "GET, POST, PUT, PATCH, DELETE, OPTION, HEAD")
  private String allowCorsRequestMethods = "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD";

  /**
   * @see HttpResponseHeader#Access_Control_Expose_Headers
   */
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(description = "Specifying allow access to certain header fields in the response that are not included in the default set of accessible headers, which can be accessed by the client (usually a web browser), default empty")
  private String allowExposeHeaders = null;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Cors cors)) {
      return false;
    }
    return Objects.equals(allowCorsOrigin, cors.allowCorsOrigin) &&
        Objects.equals(allowCorsCredentials, cors.allowCorsCredentials) &&
        Objects.equals(allowCorsRequestHeaders, cors.allowCorsRequestHeaders) &&
        Objects.equals(allowCorsRequestMethods, cors.allowCorsRequestMethods) &&
        Objects.equals(allowExposeHeaders, cors.allowExposeHeaders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allowCorsOrigin, allowCorsCredentials, allowCorsRequestHeaders,
        allowCorsRequestMethods, allowExposeHeaders);
  }
}
