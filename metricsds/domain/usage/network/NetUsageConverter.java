package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage.Network;
import cloud.xcan.angus.spec.experimental.CsvConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NetUsageConverter implements AttributeConverter<CsvConverter<Network>, String> {

  @Override
  public String convertToDatabaseColumn(CsvConverter usage) {
    return isEmpty(usage) ? null : usage.toString();
  }

  @Override
  public Network convertToEntityAttribute(String data) {
    return isEmpty(data) ? null : new Network().fromString(data);
  }

}
