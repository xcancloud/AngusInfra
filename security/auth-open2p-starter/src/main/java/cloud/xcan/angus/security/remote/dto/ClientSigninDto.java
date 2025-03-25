package cloud.xcan.angus.security.remote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ClientSigninDto {

  @NotBlank
  @Length(max = 80)
  @Schema(example = "xcan_tp", requiredMode = RequiredMode.REQUIRED)
  private String clientId;

  @NotBlank
  @Length(min = 10, max = 160)
  @Schema(example = "6917ae827c964acc8dd7638fe0581b67", requiredMode = RequiredMode.REQUIRED)
  private String clientSecret;

  @NotBlank
  @Length(max = 32)
  @Schema(example = "sign", requiredMode = RequiredMode.REQUIRED)
  private String scope;

}
