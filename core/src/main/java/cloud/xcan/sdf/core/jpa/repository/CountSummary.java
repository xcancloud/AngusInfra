package cloud.xcan.sdf.core.jpa.repository;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CountSummary {

  private String key;

  private long total;

  public CountSummary() {
  }

  public CountSummary(long total) {
    this.total = total;
  }

  public CountSummary(String key, long total) {
    this.key = key;
    this.total = total;
  }
}
