package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class UserName implements Serializable {

  @Schema(description = "User id")
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @Schema(description = "User fullname", maxLength = DEFAULT_NAME_LENGTH)
  private String fullname;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserName userName)) {
      return false;
    }
    return Objects.equals(id, userName.id) &&
        Objects.equals(fullname, userName.fullname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, fullname);
  }
}
