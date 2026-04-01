package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Memory;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MemoryUsageConverter implements AttributeConverter<CsvConverter<Memory>, String> {

  @Override
  public String convertToDatabaseColumn(CsvConverter usage) {
    return isNull(usage) ? null : usage.toString();
  }

  @Override
  public Memory convertToEntityAttribute(String data) {
    return isNull(data) ? null : new Memory().fromString(data);
  }

}
