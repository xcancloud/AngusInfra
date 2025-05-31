package cloud.xcan.angus.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import org.springframework.util.LinkedMultiValueMap;

public class MultiValueMapSerializer extends JsonSerializer<LinkedMultiValueMap> {

  private final boolean simplifySingleElement;

  public MultiValueMapSerializer() {
    this(true);
  }

  public MultiValueMapSerializer(boolean simplifySingleElement) {
    this.simplifySingleElement = simplifySingleElement;
  }

  @Override
  public void serialize(LinkedMultiValueMap map,
      JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    for (Object key : map.keySet()) {
      List<String> values = map.get(key);

      if (simplifySingleElement && values.size() == 1) {
        gen.writeStringField(key.toString(), values.get(0));
      } else {
        gen.writeArrayFieldStart(key.toString());
        for (String value : values) {
          gen.writeString(value);
        }
        gen.writeEndArray();
      }
    }
    gen.writeEndObject();
  }
}
