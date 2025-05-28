package cloud.xcan.angus.api.pojo.auth;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_PARAM_VALUE_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;

import cloud.xcan.angus.api.enums.AuthIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class SimpleHttpAuth {

  @NotNull
  @Schema(description = "The location of the API key. Authentication parameter location, Valid values are `header` or `query`",
      example = "header", requiredMode = RequiredMode.REQUIRED)
  private AuthIn in;

  @NotEmpty
  @Length(max = MAX_PARAM_NAME_LENGTH)
  @Schema(description = "The name of the API key. The name of the header, query parameter to be used, for example: Authorization.",
      example = "Authorization", requiredMode = RequiredMode.REQUIRED)
  private String keyName;

  @NotEmpty
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(description = "The value of the API key. Authentication parameter value, for example: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
      example = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", requiredMode = RequiredMode.REQUIRED)
  private String value;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SimpleHttpAuth that)) {
      return false;
    }
    return in == that.in &&
        Objects.equals(keyName, that.keyName) &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, keyName, value);
  }
}
