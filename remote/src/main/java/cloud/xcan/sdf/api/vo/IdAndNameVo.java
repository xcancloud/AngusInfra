package cloud.xcan.sdf.api.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class IdAndNameVo {

  private Long id;
  private String name;
}
