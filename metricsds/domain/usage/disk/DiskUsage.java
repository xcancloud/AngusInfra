package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk;

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
@Table(name = "node_disk_usage")
@Setter
@Getter
@Accessors(chain = true)
public class DiskUsage extends EntitySupport<DiskUsage, Long> {

  @Id
  private Long id;

  private Long timestamp;

  @Column(name = "node_id")
  private long nodeId;

  @Column(name = "device_name")
  private String deviceName;

  @Convert(converter = DiskUsageConverter.class)
  private Disk disk;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class Disk implements CsvConverter<Disk> {

    private long total;

    // Unsupported in oshi
    @Deprecated
    private long free;
    @Deprecated
    private long used;
    @Deprecated
    private long avail;
    @Deprecated
    private double usePercent;

    private double readsRate;
    private double writesRate;
    private double readBytesRate;
    private double writeBytesRate;

    @Override
    public String toString() {
      return total + "," + free + "," + used + "," + avail + "," + usePercent + ","
          + readsRate + "," + writesRate + "," + readBytesRate + "," + writeBytesRate;
    }

    public String toHumanString() {
      DecimalFormat format = new DecimalFormat("0.00");
      DecimalFormat toKBFormat = new DecimalFormat("0.000");
      return total + "," + free + "," + used + "," + avail + ","
          + format.format(usePercent) + ","
          + format.format(readsRate) + ","  // Read IOPS
          + format.format(writesRate) + "," // Write IOPS
          + toKBFormat.format(readBytesRate / 1024 / 1024) + "," // Read MB/s
          + toKBFormat.format(writeBytesRate / 1024 / 1024); // Write MB/s
    }

    @Override
    public Disk fromString(String... cvs) {
      String[] usages = cvs[0].split(",");
      total = Long.parseLong(usages[0]);
      free = Long.parseLong(usages[1]);
      used = Long.parseLong(usages[2]);
      avail = Long.parseLong(usages[3]);
      usePercent = Double.parseDouble(usages[4]);
      readsRate = Double.parseDouble(usages[5]);
      writesRate = Double.parseDouble(usages[6]);
      readBytesRate = Double.parseDouble(usages[7]);
      writeBytesRate = Double.parseDouble(usages[8]);
      return this;
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
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiskUsage diskUsage = (DiskUsage) o;
    return Objects.equals(id, diskUsage.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
