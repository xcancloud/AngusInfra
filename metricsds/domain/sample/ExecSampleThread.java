package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class ExecSampleThread extends EntitySupport<ExecSampleThread, Long>
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

  @Column(name = "tenant_id")
  private Long tenantId;

  /**
   * threadPoolSize,threadPoolActiveSize,threadMaxPoolSize,threadRunning,threadTerminated
   */
  public String toCsvStringValue() {
    return new StringJoiner(",")
        .add("" + threadPoolSize)
        .add("" + threadPoolActiveSize)
        .add("" + threadMaxPoolSize)
        .add("" + threadRunning)
        .add("" + threadTerminated)
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
    if (!(o instanceof ExecSampleThread that)) {
      return false;
    }
    return threadPoolSize == that.threadPoolSize &&
        threadPoolActiveSize == that.threadPoolActiveSize &&
        threadMaxPoolSize == that.threadMaxPoolSize &&
        threadRunning == that.threadRunning &&
        threadTerminated == that.threadTerminated &&
        Objects.equals(id, that.id) &&
        Objects.equals(execId, that.execId) &&
        Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(timestamp0, that.timestamp0) &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, execId, nodeId, timestamp, timestamp0, name, threadPoolSize,
        threadPoolActiveSize, threadMaxPoolSize, threadRunning, threadTerminated);
  }
}
