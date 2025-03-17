package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.utils.SQLParameterReplacer.replaceParameters;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class SQLParameterReplacerUtils {

  @Test
  public void test() {
    String sql = "SELECT * FROM table WHERE column1 = :value1 AND column2 = :value2";
    Map<String, Object> parameters = Map.of("value1", "123", "value2", "456");
    String replacedSQL = replaceParameters(sql, parameters);
    Assert.assertEquals("SELECT * FROM table WHERE column1 = 123 AND column2 = 456", replacedSQL);
  }
}
