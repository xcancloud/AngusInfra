package cloud.xcan.sdf.api.pojo;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class Attachment {

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @Schema(description = "Attachment file name", maxLength = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @Length(max = DEFAULT_URL_LENGTH_X4)
  @Schema(description = "Attachment file URL address", maxLength = DEFAULT_URL_LENGTH_X4)
  private String url;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Attachment that = (Attachment) o;
    return name.equals(that.name) && url.equals(that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url);
  }
}
