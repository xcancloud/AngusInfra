package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.api.enums.AuthObjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class AuthObjectInfo {

  @Schema(description = "Auth object type")
  private AuthObjectType type;

  @Schema(description = "Auth object id")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Auth object name", maxLength = MAX_NAME_LENGTH)
  private String name;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Auth object avatar", maxLength = MAX_URL_LENGTH_X4)
  private String avatar;

}
