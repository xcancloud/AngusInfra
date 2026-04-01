package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.model.SampleResult;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@ShardingTable
@Entity
@Table(name = "exec_sample_content")
@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleContent extends EntitySupport<ExecSampleContent, Long> {

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
   * Supported values: SampleResultContent、ExtContent1、ExtContent2.
   */
  @Column(name = "ext_field")
  private String extField;

  /**
   * Number of iterations during functional testing and debugging
   */
  private Long iteration;

  /**
   * Sampling id.
   */
  @Column(name = "`key`")
  private String key;

  /**
   * Sampling content.
   */
  //@Type(JsonType.class)
  //@Column(columnDefinition = "json", name = "content")
  private String content;
  ////////////////////////////Sampling Fields///////////////////////////

  @Column(name = "tenant_id")
  private Long tenantId;

  @Transient
  private SampleResult sampleResult;

  @SneakyThrows
  @JsonIgnore
  public SampleResult getSampleResult() {
    if (nonNull(sampleResult)) { // Cache
      return sampleResult;
    }
    sampleResult = isNotEmpty(content) ? JsonUtils.convert(content, SampleResult.class) : null;
    return sampleResult;
  }

  @Override
  public Long identity() {
    return id;
  }
}
