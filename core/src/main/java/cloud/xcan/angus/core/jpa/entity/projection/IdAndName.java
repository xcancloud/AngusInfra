package cloud.xcan.angus.core.jpa.entity.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Used to project(projection) id and name fields when returning from a query.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IdAndName {

  private Long id;

  private String name;

}
