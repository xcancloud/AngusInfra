package cloud.xcan.angus.spec.utils;

import java.util.Map;

public class SQLParameterReplacer {

  public static String replaceParameters(String sql, Map<String, ?> parameters) {
    for (Map.Entry<String, ?> entry : parameters.entrySet()) {
      String parameterName = ":" + entry.getKey();
      String parameterValue = entry.getValue().toString();
      sql = sql.replace(parameterName, parameterValue);
    }
    return sql;
  }

}
