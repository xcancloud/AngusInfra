package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ShardingTable
@Entity
@Table(name = "exec_sample_error_cause")
@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleErrorCause extends EntitySupport<ExecSampleErrorCause, Long> {

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
   * Sampling id.
   */
  @Column(name = "`key`")
  private String key;

  /**
   * Sampling error content.
   */
  private String content;
  ////////////////////////////Sampling Fields///////////////////////////

  @Column(name = "tenant_id")
  private Long tenantId;

  @Override
  public Long identity() {
    return id;
  }
}

