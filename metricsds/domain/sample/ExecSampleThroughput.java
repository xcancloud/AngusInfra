package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ShardingTable
@Entity
@Table(name = "exec_sample")
@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleThroughput extends EntitySupport<ExecSampleThroughput, Long>
    implements ExecSampleMergeBase {

  @Id
  private Long id;

  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "node_id")
  private Long nodeId;

  /**
   * The server records the sampling time.
   */
  @Column(columnDefinition = "TIMESTAMP")
  private Long timestamp;

  ////////////////////////////Sampling Fields///////////////////////////
  /**
   * The runner records the sampling time.
   */
  @Column(columnDefinition = "TIMESTAMP")
  private Long timestamp0;

  /**
   * Sampling task name.
   */
  private String name;

  /**
   * ======Throughput======
   * <p>
   * Very target or target transaction group will be sampled
   */
  private double ops;

  private double tps;

  private double brps;

  private double bwps;

  @Column(name = "tenant_id")
  private Long tenantId;

  /**
   * ops,tps,brps,bwps
   */
  public String toCsvStringValue() {
    return new StringJoiner(",")
        .add("" + BigDecimal.valueOf(this.ops).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.brps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.bwps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .toString();
  }

  @Override
  public Long identity() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExecSampleThroughput)) {
      return false;
    }
    ExecSampleThroughput that = (ExecSampleThroughput) o;
    return Double.compare(that.ops, ops) == 0 &&
        Double.compare(that.tps, tps) == 0 &&
        Double.compare(that.brps, brps) == 0 &&
        Double.compare(that.bwps, bwps) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(execId, that.execId) &&
        Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(timestamp0, that.timestamp0) &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, execId, nodeId, timestamp, timestamp0, name, ops, tps, brps, bwps);
  }
}
