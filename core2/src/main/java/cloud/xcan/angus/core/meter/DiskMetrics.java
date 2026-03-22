package cloud.xcan.angus.core.meter;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.File;

public class DiskMetrics implements MeterBinder {

  private final File rootFilePath;

  public DiskMetrics() {
    this.rootFilePath = new File(".");
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    Gauge.builder("diskspace.total", rootFilePath, File::getTotalSpace)
        .register(registry);
    Gauge.builder("diskspace.free", rootFilePath, File::getFreeSpace)
        .register(registry);
    Gauge.builder("diskspace.usage", rootFilePath, c -> {
      long totalDiskSpace = rootFilePath.getTotalSpace();
      if (totalDiskSpace == 0) {
        return 0.0;
      }

      long usedDiskSpace = totalDiskSpace - rootFilePath.getFreeSpace();
      return (double) usedDiskSpace / totalDiskSpace * 100;
    }).register(registry);
  }
}
