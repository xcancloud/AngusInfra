package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage;

import static java.util.Objects.isNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Cpu;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CpuUsageConverter implements AttributeConverter<CsvConverter<Cpu>, String> {

  @Override
  public String convertToDatabaseColumn(CsvConverter usage) {
    return isNull(usage) ? null : usage.toString();
  }

  @Override
  public Cpu convertToEntityAttribute(String data) {
    return isNull(data) ? null : new Cpu().fromString(data);
  }

}
