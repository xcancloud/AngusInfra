package cloud.xcan.sdf.spec.utils;

import java.util.Map;

public class SQLParameterReplacer {

  public static String replaceParameters(String sql, Map<String, Object> parameters) {
    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
      String parameterName = ":" + entry.getKey();
      String parameterValue = entry.getValue().toString();
      sql = sql.replace(parameterName, parameterValue);
    }
    return sql;
  }

}
