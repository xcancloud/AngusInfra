package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import cloud.xcan.angus.spec.unit.TimeValue;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.text.DecimalFormat;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@ShardingTable
@Entity
@Table(name = "exec_sample")
@Setter
@Getter
@Accessors(chain = true)
public class ExecSample extends EntitySupport<ExecSample, Long> implements ExecSampleMergeBase {

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
  private Long timestamp;

  ////////////////////////////Sampling Fields///////////////////////////
  /**
   * The runner records the sampling time.
   */
  private Long timestamp0;

  /**
   * Sampling task name.
   */
  private String name;

  /**
   * The number of ramp-up and ramp-down threads.
   */
  @Column(name = "ramp_num")
  private int rampNum;

  /**
   * =========Time=========
   */
  private long duration;

  private long duration0;

  @Column(name = "start_time")
  private long startTime;

  @Column(name = "end_time")
  private long endTime;

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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "error_cause_counter")
  private Map<String, Long> errorCauseCounter;

  /**
   * ========Threads=======
   */
  @Column(name = "thread_pool_size")
  private int threadPoolSize;

  @Column(name = "thread_pool_active_size")
  private int threadPoolActiveSize;

  @Column(name = "thread_max_pool_size")
  private int threadMaxPoolSize;

  @Column(name = "thread_running")
  private boolean threadRunning;

  @Column(name = "thread_terminated")
  private boolean threadTerminated;

  /**
   * ========UploadResultProgress=======
   */
  @Column(name = "upload_result_bytes")
  private long uploadResultBytes;

  @Column(name = "upload_result_total_bytes")
  private long uploadResultTotalBytes;

  /**
   * ========Extension====
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "ext_counter_map1")
  private Map<String, Long> extCounterMap1;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "ext_counter_map2")
  private Map<String, Long> extCounterMap2;

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
  private TimeValue durationUnit;

  public String getUploadResultProgress() {
    DecimalFormat format = new DecimalFormat("0.00");
    return uploadResultTotalBytes > 0 ? format.format(
        (uploadResultBytes / (double) uploadResultTotalBytes) * 100) : null;
  }

  @Override
  public Long identity() {
    return id;
  }

}
