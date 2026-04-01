package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.FileSystem;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FileSystemUsageConverter implements
    AttributeConverter<CsvConverter<FileSystem>, String> {


  @Override
  public String convertToDatabaseColumn(CsvConverter usage) {
    return isNull(usage) ? null : usage.toString();
  }

  @Override
  public FileSystem convertToEntityAttribute(String data) {
    return isNull(data) ? null : new FileSystem().fromString(data);
  }

}
