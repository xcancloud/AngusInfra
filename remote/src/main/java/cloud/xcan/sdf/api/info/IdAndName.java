package cloud.xcan.sdf.api.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class IdAndName {

  private Long id;
  private String name;
}
