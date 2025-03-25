package cloud.xcan.angus.api.pojo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class IdAndCreatedDate implements IdAndCreatedDateBase<IdAndCreatedDate> {

  protected Long id;

  protected LocalDateTime createdDate;

}
