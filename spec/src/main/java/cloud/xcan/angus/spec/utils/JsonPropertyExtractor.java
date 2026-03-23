package cloud.xcan.angus.spec.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JsonPropertyExtractor {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private JsonPropertyExtractor() {
  }

  /**
   * Extract all values of the specified property from a JSON string.
   *
   * @param jsonStr   JSON string
   * @param targetKey Target property name
   * @return A list containing all matching values (automatically handles primitive types and nested
   * structures)
   */
  public static List<Object> extractValues(String jsonStr, String targetKey) {
    Objects.requireNonNull(targetKey, "targetKey");
    List<Object> result = new ArrayList<>();
    if (jsonStr == null || jsonStr.isEmpty()) {
      return result;
    }

    try {
      JsonNode rootNode = MAPPER.readTree(jsonStr);
      traverseJsonNode(rootNode, targetKey, result);
    } catch (Exception e) {
      throw new IllegalArgumentException("Parse json string exception", e);
    }
    return result;
  }

  private static void traverseJsonNode(JsonNode node, String targetKey, List<Object> result) {
    if (node.isObject()) {
      handleObjectNode(node, targetKey, result);
    } else if (node.isArray()) {
      handleArrayNode(node, targetKey, result);
    }
  }

  private static void handleObjectNode(JsonNode node, String targetKey, List<Object> result) {
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      String currentKey = entry.getKey();
      JsonNode valueNode = entry.getValue();

      if (currentKey.equals(targetKey)) {
        result.add(convertJsonValue(valueNode));
      }

      traverseJsonNode(valueNode, targetKey, result);
    }
  }

  private static void handleArrayNode(JsonNode arrayNode, String targetKey, List<Object> result) {
    for (JsonNode element : arrayNode) {
      traverseJsonNode(element, targetKey, result);
    }
  }

  private static Object convertJsonValue(JsonNode valueNode) {
    if (valueNode.isTextual()) {
      return valueNode.asText();
    } else if (valueNode.isNumber()) {
      return valueNode.numberValue();
    } else if (valueNode.isBoolean()) {
      return valueNode.asBoolean();
    } else if (valueNode.isNull()) {
      return null;
    } else if (valueNode.isObject() || valueNode.isArray()) {
      return valueNode.toString();
    }
    return null;
  }
}
