package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import cloud.xcan.angus.spec.unit.TimeValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
public class ExecSampleSummary extends EntitySupport<ExecSampleSummary, Long>
    implements ExecSampleMergeBase {

  @Id
  private Long id;

  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "node_id")
  private Long nodeId;

  private boolean finish;

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
   * =========Time=========
   */
  private long duration;

  //@Column(name = "start_time")
  //private long startTime;

  //@Column(name = "end_time")
  //private long endTime;

  /**
   * =======Counter=======
   */
  private long errors;

  private Long iterations;

  private long n;

  private long operations;

  private long transactions;

  @Column(name = "read_bytes")
  private long readBytes;

  @Column(name = "write_bytes")
  private long writeBytes;

  /**
   * ======Throughput======
   * <p>
   * Very target or target transaction group will be sampled
   */
  private double ops;

  private double tps;

  private double brps;

  private double bwps;

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

  /**
   * ========Error========
   */
  @Column(name = "error_rate")
  private double errorRate;

  //@Type(JsonType.class)
  //@Column(columnDefinition = "json", name = "error_cause_counter")
  //private Map<String, Long> errorsCounter;

  /**
   * ========Threads=======
   */
  @Column(name = "thread_pool_size")
  private int threadPoolSize;

  @Column(name = "thread_pool_active_size")
  private int threadPoolActiveSize;

  @Column(name = "thread_max_pool_size")
  private int threadMaxPoolSize;

//  @Column(name = "thread_running")
//  private boolean threadRunning;
//
//  @Column(name = "thread_terminated")
//  private boolean threadTerminated;

//  /**
//   * ========UploadResultProgress=======
//   */
//  @Column(name = "upload_result_bytes")
//  private long uploadResultBytes;
//
//  @Column(name = "upload_result_total_bytes")
//  private long uploadResultTotalBytes;

  /**
   * ========Extension====
   */
  @Column(name = "ext_counter1")
  private long extCounter1;

  @Column(name = "ext_counter2")
  private long extCounter2;

  @Column(name = "ext_gauge1")
  private double extGauge1;

  @Column(name = "ext_gauge2")
  private double extGauge2;

  ////////////////////////////Sampling Fields///////////////////////////

  @Column(name = "tenant_id")
  private Long tenantId;

  @Transient
  private long iterations0;
  @Transient
  private TimeValue duration0;

  /**
   * duration,errors,iterations,n,operations,transactions,readBytes,writeBytes,ops,tps,brps,bwps,tranMean,tranMin,tranMax,tranP50,tranP75,tranP90,tranP95,tranP99,tranP999,errorRate,extCounter1,extCounter2,extCounter3,
   * extGauge1,extGauge2,extGauge3
   */
  public String toCsvStringValue() {
    return new StringJoiner(",")
        .add("" + duration)
        .add("" + errors)
        .add("" + iterations)
        .add("" + n)
        .add("" + operations)
        .add("" + transactions)
        .add("" + readBytes)
        .add("" + writeBytes)
        .add("" + BigDecimal.valueOf(this.ops).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.brps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.bwps).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranMean).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + tranMin)
        .add("" + tranMax)
        .add("" + BigDecimal.valueOf(this.tranP50).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP75).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP90).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP95).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP99).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + BigDecimal.valueOf(this.tranP999).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add(
            "" + BigDecimal.valueOf(this.errorRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .add("" + threadPoolSize)
        .add("" + threadPoolActiveSize)
        .add("" + threadMaxPoolSize)
        .add("" + extCounter1)
        .add("" + extCounter2)
        .add("" + extGauge1)
        .add("" + extGauge2)
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
    if (!(o instanceof ExecSampleSummary)) {
      return false;
    }
    ExecSampleSummary that = (ExecSampleSummary) o;
    return finish == that.finish &&
        duration == that.duration &&
        errors == that.errors &&
        n == that.n &&
        operations == that.operations &&
        transactions == that.transactions &&
        readBytes == that.readBytes &&
        writeBytes == that.writeBytes &&
        Double.compare(that.ops, ops) == 0 &&
        Double.compare(that.tps, tps) == 0 &&
        Double.compare(that.brps, brps) == 0 &&
        Double.compare(that.bwps, bwps) == 0 &&
        Double.compare(that.tranMean, tranMean) == 0 &&
        Double.compare(that.tranMin, tranMin) == 0 &&
        Double.compare(that.tranMax, tranMax) == 0 &&
        Double.compare(that.tranP50, tranP50) == 0 &&
        Double.compare(that.tranP75, tranP75) == 0 &&
        Double.compare(that.tranP90, tranP90) == 0 &&
        Double.compare(that.tranP95, tranP95) == 0 &&
        Double.compare(that.tranP99, tranP99) == 0 &&
        Double.compare(that.tranP999, tranP999) == 0 &&
        Double.compare(that.errorRate, errorRate) == 0 &&
        threadPoolSize == that.threadPoolSize &&
        threadPoolActiveSize == that.threadPoolActiveSize &&
        threadMaxPoolSize == that.threadMaxPoolSize &&
        extCounter1 == that.extCounter1 &&
        extCounter2 == that.extCounter2 &&
        Double.compare(that.extGauge1, extGauge1) == 0 &&
        Double.compare(that.extGauge2, extGauge2) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(execId, that.execId) &&
        Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(timestamp0, that.timestamp0) &&
        Objects.equals(name, that.name) &&
        Objects.equals(iterations, that.iterations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, execId, nodeId, finish, timestamp, timestamp0, name, duration, errors,
        iterations, n, operations, transactions, readBytes, writeBytes, ops, tps, brps, bwps,
        tranMean, tranMin, tranMax, tranP50, tranP75, tranP90, tranP95, tranP99, tranP999,
        errorRate, threadPoolSize, threadPoolActiveSize, threadMaxPoolSize, extCounter1,
        extCounter2, extGauge1, extGauge2);
  }
}
