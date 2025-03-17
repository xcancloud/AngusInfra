package cloud.xcan.sdf.spec.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonPropertyExtractor {

  /**
   * Extract all values of the specified property from a JSON string.
   *
   * @param jsonStr   JSON string
   * @param targetKey Target property name
   * @return A list containing all matching values (automatically handles primitive types and nested
   * structures)
   */
  public static List<Object> extractValues(String jsonStr, String targetKey) {
    List<Object> result = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    try {
      JsonNode rootNode = mapper.readTree(jsonStr);
      traverseJsonNode(rootNode, targetKey, result);
    } catch (Exception e) {
      throw new IllegalArgumentException("Parse json string exception", e);
    }
    return result;
  }

  /**
   * Recursively traverse JSON nodes
   */
  private static void traverseJsonNode(JsonNode node, String targetKey, List<Object> result) {
    if (node.isObject()) {
      handleObjectNode(node, targetKey, result);
    } else if (node.isArray()) {
      handleArrayNode(node, targetKey, result);
    }
    // No need to handle other types (value nodes)
  }

  /**
   * Handle JSON object nodes
   */
  private static void handleObjectNode(JsonNode node, String targetKey, List<Object> result) {
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      String currentKey = entry.getKey();
      JsonNode valueNode = entry.getValue();

      // If the current key matches the target key, extract the value
      if (currentKey.equals(targetKey)) {
        result.add(convertJsonValue(valueNode));
      }

      // Recursively process value nodes (regardless of whether they match the key)
      traverseJsonNode(valueNode, targetKey, result);
    }
  }

  /**
   * Handle JSON array nodes
   */
  private static void handleArrayNode(JsonNode arrayNode, String targetKey, List<Object> result) {
    for (JsonNode element : arrayNode) {
      traverseJsonNode(element, targetKey, result);
    }
  }

  /**
   * Convert JsonNode to Java object
   */
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
      return valueNode.toString(); // Return the original JSON string
    }
    return null;
  }

  public static void main(String[] args) {
    String json = "{"
        + "\"number\": 1,"
        + "\"name\": \"John\","
        + "\"age\": 30,"
        + "\"address\": {"
        + "    \"street\": \"Main St\","
        + "    \"number\": 123"
        + "},"
        + "\"phoneNumbers\": ["
        + "    {\"type\": \"home\", \"number\": \"555-1234\"},"
        + "    {\"type\": \"work\", \"number\": \"555-5678\"}"
        + "],"
        + "\"properties\": {"
        + "    \"key\": \"value\","
        + "    \"nested\": {"
        + "        \"key\": \"anotherValue\""
        + "    }"
        + "}"
        + "}";

    List<Object> values = extractValues(json, "number");
    System.out.println(values);
    // Output: [123, "555-1234", "555-5678"]
  }
}
