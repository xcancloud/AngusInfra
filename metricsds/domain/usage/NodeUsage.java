package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "node_usage")
@Setter
@Getter
@Accessors(chain = true)
public class NodeUsage extends EntitySupport<NodeUsage, Long> {

  @Id
  private Long id;

  private Long timestamp;

  @Column(name = "node_id")
  private Long nodeId;

  @Convert(converter = CpuUsageConverter.class)
  private Cpu cpu;

  @Convert(converter = MemoryUsageConverter.class)
  private Memory memory;

  @Convert(converter = FileSystemUsageConverter.class)
  private FileSystem filesystem;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class Cpu implements CsvConverter<Cpu> {

    private double idle;
    private double sys;
    private double user;
    private double wait_;
    private double other;
    private double total;

    @Override
    public String toString() {
      DecimalFormat format = new DecimalFormat("0.00");
      return format.format(idle) + "," + format.format(sys) + "," + format.format(user) + ","
          + format.format(wait_) + "," + format.format(other) + "," + format.format(total);
    }

    @Override
    public Cpu fromString(String... cvs) {
      if (isEmpty(cvs)) {
        return null;
      }
      String[] usages = cvs[0].split(",");
      idle = Double.parseDouble(usages[0]);
      sys = Double.parseDouble(usages[1]);
      user = Double.parseDouble(usages[2]);
      wait_ = Double.parseDouble(usages[3]);
      other = Double.parseDouble(usages[4]);
      total = Double.parseDouble(usages[5]);
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Cpu)) {
        return false;
      }
      Cpu cpu = (Cpu) o;
      return Double.compare(cpu.idle, idle) == 0 &&
          Double.compare(cpu.sys, sys) == 0 &&
          Double.compare(cpu.user, user) == 0 &&
          Double.compare(cpu.wait_, wait_) == 0 &&
          Double.compare(cpu.other, other) == 0 &&
          Double.compare(cpu.total, total) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(idle, sys, user, wait_, other, total);
    }
  }

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class Memory implements CsvConverter<Memory> {

    // physical memory
    private long free;
    private long used;
    private double freePercent;
    private double usedPercent;

    // physical memory and swap
    private double actualFree;
    private double actualUsed;
    private double actualFreePercent;
    private double actualUsedPercent;

    // swap
    private long swapFree;
    private long swapUsed;

    @Override
    public String toString() {
      DecimalFormat format = new DecimalFormat("0.00");
      return free + "," + used + "," + format.format(freePercent) + "," + format
          .format(usedPercent) + "," + format.format(actualFree) + "," + format
          .format(actualUsed) + "," + format.format(actualFreePercent) + "," + format
          .format(actualUsedPercent) + "," + swapFree + "," + swapUsed;
    }

    @Override
    public Memory fromString(String... cvs) {
      if (isEmpty(cvs)) {
        return null;
      }
      String[] usages = cvs[0].split(",");
      free = Long.parseLong(usages[0]);
      used = Long.parseLong(usages[1]);
      freePercent = Double.parseDouble(usages[2]);
      usedPercent = Double.parseDouble(usages[3]);
      actualFree = Double.parseDouble(usages[4]);
      actualUsed = Double.parseDouble(usages[5]);
      actualFreePercent = Double.parseDouble(usages[6]);
      actualUsedPercent = Double.parseDouble(usages[7]);
      swapFree = Long.parseLong(usages[8]);
      swapUsed = Long.parseLong(usages[9]);
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Memory)) {
        return false;
      }
      Memory memory = (Memory) o;
      return free == memory.free &&
          used == memory.used &&
          Double.compare(memory.freePercent, freePercent) == 0 &&
          Double.compare(memory.usedPercent, usedPercent) == 0 &&
          Double.compare(memory.actualFree, actualFree) == 0 &&
          Double.compare(memory.actualUsed, actualUsed) == 0 &&
          Double.compare(memory.actualFreePercent, actualFreePercent) == 0 &&
          Double.compare(memory.actualUsedPercent, actualUsedPercent) == 0 &&
          swapFree == memory.swapFree &&
          swapUsed == memory.swapUsed;
    }

    @Override
    public int hashCode() {
      return Objects
          .hash(free, used, freePercent, usedPercent, actualFree, actualUsed, actualFreePercent,
              actualUsedPercent, swapFree, swapUsed);
    }
  }

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class FileSystem implements CsvConverter<FileSystem> {

    private long free;
    private long used;
    private long avail;
    /**
     * fs.used/host.fs.total
     */
    private double usedPercent;

    @JsonIgnore
    public double getFreePercent() {
      return 1 - usedPercent;
    }

    @Override
    public String toString() {
      DecimalFormat format = new DecimalFormat("0.00");
      return free + "," + used + "," + avail + "," + format.format(usedPercent);
    }

    @Override
    public FileSystem fromString(String... cvs) {
      if (isEmpty(cvs)) {
        return null;
      }
      String[] usages = cvs[0].split(",");
      free = Long.parseLong(usages[0]);
      used = Long.parseLong(usages[1]);
      avail = Long.parseLong(usages[2]);
      usedPercent = Double.parseDouble(usages[3]);
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FileSystem)) {
        return false;
      }
      FileSystem that = (FileSystem) o;
      return free == that.free &&
          used == that.used &&
          avail == that.avail &&
          Double.compare(that.usedPercent, usedPercent) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(free, used, avail, usedPercent);
    }
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeUsage)) {
      return false;
    }
    NodeUsage nodeUsage = (NodeUsage) o;
    return Objects.equals(id, nodeUsage.id) &&
        Objects.equals(timestamp, nodeUsage.timestamp) &&
        Objects.equals(nodeId, nodeUsage.nodeId) &&
        Objects.equals(cpu, nodeUsage.cpu) &&
        Objects.equals(memory, nodeUsage.memory) &&
        Objects.equals(filesystem, nodeUsage.filesystem) &&
        Objects.equals(tenantId, nodeUsage.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, timestamp, nodeId, cpu, memory, filesystem, tenantId);
  }
}
