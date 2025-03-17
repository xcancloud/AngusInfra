package cloud.xcan.sdf.spec.jackson.serializer;

import cloud.xcan.sdf.api.pojo.Pair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@JsonComponent -> Turn off global formatting configuration
public class PairToMapSerializer extends JsonSerializer<List<Pair<String, String>>> {

  @Override
  public void serialize(List<Pair<String, String>> pairs, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    Map<String, String> map = new LinkedHashMap<>();
    for (Pair<String, String> pair : pairs) {
      map.put(pair.getKey(), pair.getValue());
    }
    jsonGenerator.writeObject(map);
  }
}
