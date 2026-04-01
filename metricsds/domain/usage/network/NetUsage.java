package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network;

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
@Table(name = "node_net_usage")
@Setter
@Getter
@Accessors(chain = true)
public class NetUsage extends EntitySupport<NetUsage, Long> {

  @Id
  private Long id;

  private Long timestamp;

  @Column(name = "node_id")
  private long nodeId;

  @Column(name = "device_name")
  private String deviceName;

  @Convert(converter = NetUsageConverter.class)
  private Network network;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class Network implements CsvConverter<Network> {

    private long rxBytes;
    private double rxBytesRate;
    private long rxErrors;
    private long txBytes;
    private double txBytesRate;

    @Override
    public String toString() {
      return rxBytes + "," + rxBytesRate + "," + rxErrors + "," + txBytes + "," + txBytesRate;
    }

    public String toHumanString() {
      DecimalFormat toKBFormat = new DecimalFormat("0.000");
      return rxBytes + ","
          + toKBFormat.format(rxBytesRate / 1024 / 1024) + ","  // Read MB/s
          + rxErrors + ","
          + txBytes + ","
          + toKBFormat.format(txBytesRate / 1024 / 1024); // Write MB/s
    }

    @Override
    public Network fromString(String... cvs) {
      String[] usages = cvs[0].split(",");
      rxBytes = Long.parseLong(usages[0]);
      rxBytesRate = Double.parseDouble(usages[1]);
      rxErrors = Long.parseLong(usages[2]);
      txBytes = Long.parseLong(usages[3]);
      txBytesRate = Double.parseDouble(usages[4]);
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
    NetUsage netUsage = (NetUsage) o;
    return Objects.equals(id, netUsage.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
