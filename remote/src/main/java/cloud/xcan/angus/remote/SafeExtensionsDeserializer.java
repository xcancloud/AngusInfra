package cloud.xcan.angus.remote;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;

/**
 * Safe deserializer for Map<String, Object> fields that prevents gadget chain deserialization attacks.
 * 
 * This deserializer restricts values in the extensions map to safe types only:
 * - String
 * - Number (Integer, Long, Double, Float, etc.)
 * - Boolean
 * - null
 * 
 * Any attempt to deserialize complex objects (lists, nested objects, etc.) will be rejected.
 */
public class SafeExtensionsDeserializer extends JsonDeserializer<Map<String, Object>> {

  private static final String[] SAFE_VALUE_TYPES = {
      "java.lang.String",
      "java.lang.Integer",
      "java.lang.Long",
      "java.lang.Double",
      "java.lang.Float",
      "java.lang.Boolean",
      "java.math.BigDecimal",
      "java.math.BigInteger"
  };

  @Override
  public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    Map<String, Object> result = new HashMap<>();

    // Parse the JSON object
    if (p.currentToken() != com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
      throw new IllegalArgumentException("Expected JSON object for extensions field");
    }

    while (p.nextToken() != com.fasterxml.jackson.core.JsonToken.END_OBJECT) {
      String fieldName = p.currentName();
      p.nextToken();

      // Deserialize and validate value
      Object value = deserializeValue(p, ctxt);
      
      if (!isValueTypeSafe(value)) {
        throw new IllegalArgumentException(
            String.format("Unsafe value type in extensions: %s for key '%s'. "
                + "Only String, Number, Boolean, and null are allowed.",
                value != null ? value.getClass().getName() : "null", fieldName));
      }

      result.put(fieldName, value);
    }

    return result;
  }

  /**
   * Deserialize a single value from the JSON parser
   */
  private Object deserializeValue(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    switch (p.currentToken()) {
      case VALUE_STRING:
        return p.getValueAsString();
      case VALUE_NUMBER_INT:
        return p.getValueAsLong();
      case VALUE_NUMBER_FLOAT:
        return p.getValueAsDouble();
      case VALUE_TRUE:
        return true;
      case VALUE_FALSE:
        return false;
      case VALUE_NULL:
        return null;
      case START_OBJECT:
      case START_ARRAY:
        throw new IllegalArgumentException(
            "Complex objects and arrays are not allowed in extensions field. "
                + "Only primitive values (String, Number, Boolean) are permitted.");
      default:
        throw new IllegalArgumentException(
            "Unexpected JSON token type: " + p.currentToken());
    }
  }

  /**
   * Check if the value type is safe for deserialization
   */
  private boolean isValueTypeSafe(Object value) {
    if (value == null) {
      return true;
    }

    String typeName = value.getClass().getName();
    for (String safeType : SAFE_VALUE_TYPES) {
      if (typeName.equals(safeType)) {
        return true;
      }
    }

    return false;
  }
}
