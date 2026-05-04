package cloud.xcan.angus.spec.jackson.serializer;

import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.util.Objects;

//@JsonComponent -> Turn off global formatting configuration
public class TimeValueSerializer extends JsonSerializer<TimeValue> implements
    ContextualSerializer {

  private ValueUnitStyle style;

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider serializerProvider,
      BeanProperty beanProperty) throws JsonMappingException {
    if (beanProperty != null) {
      if (Objects.equals(beanProperty.getType().getRawClass(), TimeValue.class)) {
        TimeValueFormat valueFormat = beanProperty.getAnnotation((TimeValueFormat.class));
        if (valueFormat == null) {
          valueFormat = beanProperty.getContextAnnotation(TimeValueFormat.class);
        }
        TimeValueSerializer sizeSerializer = new TimeValueSerializer();
        if (valueFormat != null) {
          sizeSerializer.style = valueFormat.value();
        }
        return sizeSerializer;
      }
      return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }
    return serializerProvider.findNullValueSerializer(beanProperty);
  }

  @Override
  public void serialize(TimeValue timeValue, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    // Merge value and unit
    jsonGenerator.writeString(
        ValueUnitStyle.HUMAN.equals(style) ? timeValue.toHumanString() : timeValue.toString());
  }

}
