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
public class ExecSampleScore extends EntitySupport<ExecSampleScore, Long> implements
    ExecSampleMergeBase {

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
   * ==Aggregation(Trans)=
   */
  @Column(name = "tran_mean")
  private double tranMean;

  @Column(name = "tran_min")
  private double tranMin;

  @Column(name = "tran_max")
  private double tranMax;

  /**
   * ==Percentile(Trans)==
   */
  @Column(name = "tran_p50")
  private double tranP50;

  @Column(name = "tran_p75")
  private double tranP75;

  @Column(name = "tran_p90")
  private double tranP90;

  @Column(name = "tran_p95")
  private double tranP95;

  @Column(name = "tran_p99")
  private double tranP99;

  @Column(name = "tran_p999")
  private double tranP999;

  @Column(name = "tenant_id")
  private Long tenantId;

  /**
   * tranMean,tranMin,tranMax,tranP50,tranP75,tranP90,tranP95,tranP99,tranP999
   */
  public String toCsvStringValue() {
    return new StringJoiner(",")
        .add("" + BigDecimal.valueOf(this.tranMean).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + tranMin)
        .add("" + tranMax)
        .add("" + BigDecimal.valueOf(this.tranP50).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP75).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP90).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP95).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP99).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP999).setScale(2, RoundingMode.HALF_UP).doubleValue())
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
    if (!(o instanceof ExecSampleScore)) {
      return false;
    }
    ExecSampleScore that = (ExecSampleScore) o;
    return Double.compare(that.tranMean, tranMean) == 0 &&
        Double.compare(that.tranMin, tranMin) == 0 &&
        Double.compare(that.tranMax, tranMax) == 0 &&
        Double.compare(that.tranP50, tranP50) == 0 &&
        Double.compare(that.tranP75, tranP75) == 0 &&
        Double.compare(that.tranP90, tranP90) == 0 &&
        Double.compare(that.tranP95, tranP95) == 0 &&
        Double.compare(that.tranP99, tranP99) == 0 &&
        Double.compare(that.tranP999, tranP999) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(execId, that.execId) &&
        Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(timestamp0, that.timestamp0) &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, execId, nodeId, timestamp, timestamp0, name, tranMean, tranMin,
        tranMax, tranP50, tranP75, tranP90, tranP95, tranP99, tranP999);
  }
}
