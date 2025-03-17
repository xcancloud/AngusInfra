package cloud.xcan.sdf.spec.jackson.serializer;

import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.pojo.Pair;
import cloud.xcan.sdf.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@JsonComponent -> Turn off global formatting configuration
public class MapToPairDeSerializer extends JsonDeserializer<List<Pair<String, String>>> {

  @Override
  public List<Pair<String, String>> deserialize(JsonParser jsonParser,
      DeserializationContext deserializationContext) throws IOException {
    TreeNode content = jsonParser.readValueAsTree();
    if (isNull(content)) {
      return null;
    }
    Map<String, String> map = JsonUtils
        .convert(content.toString(), new TypeReference<Map<String, String>>() {
        });
    assert map != null;
    return map.entrySet().stream().map(x -> Pair.of(x.getKey(), x.getValue()))
        .collect(Collectors.toList());
  }
}
