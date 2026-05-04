package cloud.xcan.angus.remote.vo;

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
