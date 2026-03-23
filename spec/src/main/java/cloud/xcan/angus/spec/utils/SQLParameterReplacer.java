package cloud.xcan.angus.spec.utils;

import java.util.Map;
import java.util.Objects;

public final class SQLParameterReplacer {

  private SQLParameterReplacer() {
  }

  /**
   * Replaces {@code :name} tokens in {@code sql} with string values from {@code parameters}. Null
   * map values are replaced with the literal {@code "null"}.
   */
  public static String replaceParameters(String sql, Map<String, ?> parameters) {
    Objects.requireNonNull(sql, "sql");
    if (parameters == null || parameters.isEmpty()) {
      return sql;
    }
    String result = sql;
    for (Map.Entry<String, ?> entry : parameters.entrySet()) {
      String key = entry.getKey();
      if (key == null) {
        continue;
      }
      String parameterName = ":" + key;
      Object val = entry.getValue();
      String parameterValue = val == null ? "null" : String.valueOf(val);
      result = result.replace(parameterName, parameterValue);
    }
    return result;
  }
}
