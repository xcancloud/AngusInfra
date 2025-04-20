package cloud.xcan.angus.security.model.remote.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CLIENT_SECRET_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ClientSignInDto {

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description =
      "OAuth2 registered client identifier. The clientId uniquely identifies an application "
          + "to the OAuth2 server, enabling authorization and token requests.",
      maxLength = MAX_NAME_LENGTH, requiredMode = RequiredMode.REQUIRED)
  private String clientId;

  @NotBlank
  @Length(max = MAX_CLIENT_SECRET_LENGTH)
  @Schema(description =
      "OAuth2 registered client secret or null if not available. The client secret securely "
          + "authenticates the application's identity, ensuring only trusted clients access protected resources.",
      maxLength = MAX_CLIENT_SECRET_LENGTH, requiredMode = RequiredMode.REQUIRED)
  private String clientSecret;

  @NotEmpty
  @Schema(description = "The scope(s) that the client may use.", requiredMode = RequiredMode.REQUIRED)
  private String scope;

}
