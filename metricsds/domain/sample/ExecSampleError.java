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
public class ExecSampleError extends EntitySupport<ExecSampleError, Long>
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
   * =======Counter=======
   */
  private long errors;

  private long n;

  private long operations;

  private long transactions;

  /**
   * ========Error========
   */
  @Column(name = "error_rate")
  private double errorRate;

  @Column(name = "tenant_id")
  private Long tenantId;

  /**
   * errors,n,operations,transactions,errorRate
   */
  public String toCsvStringValue() {
    return new StringJoiner(",")
        .add("" + errors)
        .add("" + n)
        .add("" + operations)
        .add("" + transactions)
        .add("" + errorRate)
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
    if (!(o instanceof ExecSampleError)) {
      return false;
    }
    ExecSampleError that = (ExecSampleError) o;
    return errors == that.errors &&
        n == that.n &&
        operations == that.operations &&
        transactions == that.transactions &&
        Double.compare(that.errorRate, errorRate) == 0 &&
        Objects.equals(id, that.id) &&
        Objects.equals(execId, that.execId) &&
        Objects.equals(nodeId, that.nodeId) &&
        Objects.equals(timestamp, that.timestamp) &&
        Objects.equals(timestamp0, that.timestamp0) &&
        Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, execId, nodeId, timestamp, timestamp0, name, errors, n, operations,
        transactions, errorRate);
  }
}
