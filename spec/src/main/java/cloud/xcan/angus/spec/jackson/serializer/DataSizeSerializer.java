package cloud.xcan.angus.spec.jackson.serializer;

import cloud.xcan.angus.spec.unit.DataSize;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.util.Objects;

//@JsonComponent -> Turn off global formatting configuration
public class DataSizeSerializer extends JsonSerializer<DataSize> implements
    ContextualSerializer {

  private ValueUnitStyle style;

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider serializerProvider,
      BeanProperty beanProperty) throws JsonMappingException {
    if (beanProperty != null) {
      if (Objects.equals(beanProperty.getType().getRawClass(), DataSize.class)) {
        DataSizeFormat valueFormat = beanProperty.getAnnotation((DataSizeFormat.class));
        if (valueFormat == null) {
          valueFormat = beanProperty.getContextAnnotation(DataSizeFormat.class);
        }
        DataSizeSerializer sizeSerializer = new DataSizeSerializer();
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
  public void serialize(DataSize dataSize, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    // Merge value and unit
    jsonGenerator.writeString(
        ValueUnitStyle.HUMAN.equals(style) ? dataSize.toHumanString() : dataSize.toString());
  }

}
