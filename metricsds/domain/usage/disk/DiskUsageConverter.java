package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage.Disk;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DiskUsageConverter implements AttributeConverter<CsvConverter<Disk>, String> {

  @Override
  public String convertToDatabaseColumn(CsvConverter usage) {
    return isNull(usage) ? null : usage.toString();
  }

  @Override
  public Disk convertToEntityAttribute(String data) {
    return isNull(data) ? null : new Disk().fromString(data);
  }

}
