package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class Attachment {

  @Schema(description = "File ID (In storage service)", accessMode = AccessMode.READ_ONLY)
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Attachment file name", maxLength = MAX_NAME_LENGTH_X2)
  private String name;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Attachment file URL address", maxLength = MAX_URL_LENGTH_X4)
  private String url;

  @Schema(description = "Attachment file size (bytes)")
  private Integer size;

  @Schema(description = "Upload user name")
  private String uploadBy;

  @Schema(description = "Upload date", maxLength = MAX_NAME_LENGTH)
  private LocalDateTime uploadAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attachment that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(url, that.url)
        && Objects.equals(size, that.size)
        && Objects.equals(uploadBy, that.uploadBy)
        && Objects.equals(uploadAt, that.uploadAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, url, size, uploadBy, uploadAt);
  }
}
