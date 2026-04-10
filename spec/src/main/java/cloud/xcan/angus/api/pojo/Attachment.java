package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

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

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Attachment file name", maxLength = MAX_NAME_LENGTH_X2)
  private String name;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Attachment file URL address", maxLength = MAX_URL_LENGTH_X4)
  private String url;

  @Schema(description = "Attachment file size")
  private Integer size;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attachment that)) {
      return false;
    }
    return Objects.equals(name, that.name)
        && Objects.equals(url, that.url)
        && Objects.equals(size, that.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, size);
  }
}
