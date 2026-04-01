package cloud.xcan.angus.core.jpa.entity.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Used to project(projection) name fields when returning from a query.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Name {

  private String name;

}
