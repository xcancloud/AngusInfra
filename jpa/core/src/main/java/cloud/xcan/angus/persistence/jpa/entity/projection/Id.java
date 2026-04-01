package cloud.xcan.angus.core.jpa.entity.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Used to project(projection) id fields when returning from a query.
 */
@Setter
@Getter
@AllArgsConstructor
@Accessors(chain = true)
public class Id {

  private Long id;

}
