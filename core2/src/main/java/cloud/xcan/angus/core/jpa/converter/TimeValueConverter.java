package cloud.xcan.angus.core.jpa.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.spec.unit.TimeValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TimeValueConverter implements AttributeConverter<TimeValue, String> {

  @Override
  public String convertToDatabaseColumn(TimeValue value) {
    return isNull(value) ? null : value.toString();
  }

  @Override
  public TimeValue convertToEntityAttribute(String value) {
    return isNull(value) ? null : TimeValue.parse(value);
  }
}
