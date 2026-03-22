package cloud.xcan.angus.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.LinkedMultiValueMap;

public class MultiValueMapDeserializer extends JsonDeserializer<LinkedMultiValueMap> {

  @Override
  public LinkedMultiValueMap<String, String> deserialize(
      JsonParser p, DeserializationContext ctx) throws IOException {
    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    JsonNode rootNode = p.readValueAsTree();

    rootNode.fields().forEachRemaining(entry -> {
      String key = entry.getKey();
      JsonNode valueNode = entry.getValue();

      if (valueNode.isArray()) {
        ArrayNode arrayNode = (ArrayNode) valueNode;
        List<String> values = new ArrayList<>(arrayNode.size());
        arrayNode.forEach(element -> values.add(element.asText()));
        map.put(key, values);
      } else if (valueNode.isValueNode()) {
        map.add(key, valueNode.asText());
      }
    });
    return map;
  }
}
