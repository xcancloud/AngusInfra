package cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.text.DecimalFormat;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ShardingTable
@Entity
@Table(name = "jvm_service_usage")
@Setter
@Getter
@Accessors(chain = true)
public class JvmServiceUsage extends EntitySupport<JvmServiceUsage, Long> {

  @Id
  @Column(name = "id")
  private Long id;

  private Long timestamp;

  @Column(name = "node_id")
  private Long nodeId;

  @Column(name = "service_id")
  private Long serviceId;

  /**
   * Sources include: Agent, MockService.
   */
  @Column(name = "source")
  private String source;

  @Convert(converter = JvmMemoryUsageConverter.class)
  @Column(name = "jvm")
  private JvmMemory jvm;

  @Convert(converter = JvmProcessorUsageConverter.class)
  @Column(name = "processor")
  private JvmProcessor processor;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Override
  public Long identity() {
    return id;
  }

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class JvmMemory implements CsvConverter<JvmMemory> {

    /**
     * An estimate of the number of buffers in the pool.
     */
    private double bufferCount;

    /**
     * An estimate of the memory that the Java virtual machine is using for this buffer pool. Unit
     * bytes.
     */
    private double bufferMemoryUsed;

    /**
     * An estimate of the total capacity of the buffers in this pool. Unit bytes.
     */
    private double bufferTotalCapacity;

    /**
     * The amount of used memory. Unit bytes.
     */
    private double memoryUsed;

    /**
     * The amount of memory in bytes that is committed for the Java virtual machine to use. Unit
     * bytes.
     */
    private double memoryCommitted;

    /**
     * The maximum amount of memory in bytes that can be used for memory management. Unit bytes.
     */
    private double memoryMax;

    @Override
    public String toString() {
      return bufferCount + "," + bufferMemoryUsed + "," + bufferTotalCapacity + ","
          + memoryUsed + "," + memoryCommitted + "," + memoryMax;
    }

    @Override
    public JvmMemory fromString(String... cvs) {
      if (isEmpty(cvs)) {
        return null;
      }
      String[] usages = cvs[0].split(",");
      bufferCount = Double.parseDouble(usages[0]);
      bufferMemoryUsed = Double.parseDouble(usages[1]);
      bufferTotalCapacity = Double.parseDouble(usages[2]);
      memoryUsed = Double.parseDouble(usages[3]);
      memoryCommitted = Double.parseDouble(usages[4]);
      memoryMax = Double.parseDouble(usages[5]);
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JvmMemory)) {
        return false;
      }
      JvmMemory jvmMemory = (JvmMemory) o;
      return Double.compare(jvmMemory.bufferCount, bufferCount) == 0 &&
          Double.compare(jvmMemory.bufferMemoryUsed, bufferMemoryUsed) == 0 &&
          Double.compare(jvmMemory.bufferTotalCapacity, bufferTotalCapacity) == 0 &&
          Double.compare(jvmMemory.memoryUsed, memoryUsed) == 0 &&
          Double.compare(jvmMemory.memoryCommitted, memoryCommitted) == 0 &&
          Double.compare(jvmMemory.memoryMax, memoryMax) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(bufferCount, bufferMemoryUsed, bufferTotalCapacity, memoryUsed,
          memoryCommitted, memoryMax);
    }
  }

  /**
   * @see io.micrometer.core.instrument.binder.system.ProcessorMetrics
   */
  @Setter
  @Getter
  @Accessors(chain = true)
  public static class JvmProcessor implements CsvConverter<JvmProcessor> {

    /**
     * The number of processors available to the Java virtual machine.
     */
    private int cpuCount;

    /**
     * The "recent cpu usage" for the whole system.
     */
    private double sysUsage;

    /**
     * The "recent cpu usage" for the Java Virtual Machine process.
     */
    private double processUsage;

    @Override
    public String toString() {
      DecimalFormat format = new DecimalFormat("0.00");
      return cpuCount + "," + format.format(sysUsage) + "," + format.format(processUsage);
    }

    @Override
    public JvmProcessor fromString(String... cvs) {
      if (isEmpty(cvs)) {
        return null;
      }
      String[] usages = cvs[0].split(",");
      cpuCount = Integer.parseInt(usages[0]);
      sysUsage = Double.parseDouble(usages[1]);
      processUsage = Double.parseDouble(usages[2]);
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JvmProcessor)) {
        return false;
      }
      JvmProcessor that = (JvmProcessor) o;
      return cpuCount == that.cpuCount &&
          Double.compare(that.sysUsage, sysUsage) == 0 &&
          Double.compare(that.processUsage, processUsage) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(cpuCount, sysUsage, processUsage);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JvmServiceUsage)) {
      return false;
    }
    JvmServiceUsage usage = (JvmServiceUsage) o;
    return Objects.equals(id, usage.id) &&
        Objects.equals(timestamp, usage.timestamp) &&
        Objects.equals(nodeId, usage.nodeId) &&
        Objects.equals(serviceId, usage.serviceId) &&
        Objects.equals(source, usage.source) &&
        Objects.equals(jvm, usage.jvm) &&
        Objects.equals(processor, usage.processor) &&
        Objects.equals(tenantId, usage.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, timestamp, nodeId, serviceId, source, jvm, processor, tenantId);
  }
}
