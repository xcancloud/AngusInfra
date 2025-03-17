package cloud.xcan.sdf.core.jpa.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LongKeyCountSummary {

  private long key;

  private long total;

}
