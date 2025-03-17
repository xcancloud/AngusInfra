package cloud.xcan.sdf.spec.jackson.serializer;

import cloud.xcan.sdf.spec.unit.DataSize;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

//@JsonComponent -> Turn off global formatting configuration
public class DataSizeDeSerializer extends JsonDeserializer<DataSize> {

  @Override
  public DataSize deserialize(JsonParser jsonParser,
      DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    return DataSize.parse(jsonParser.getText());
  }
}
