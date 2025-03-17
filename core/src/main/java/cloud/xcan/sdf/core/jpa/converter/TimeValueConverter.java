package cloud.xcan.sdf.core.jpa.converter;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.spec.unit.TimeValue;
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
