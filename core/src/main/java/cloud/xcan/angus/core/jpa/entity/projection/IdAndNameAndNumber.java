package cloud.xcan.angus.core.jpa.entity.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Used to project(projection) id、name and num fields when returning from a query.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IdAndNameAndNumber {

  private Long id;

  private String name;

  private Long num;

}
