package cloud.xcan.angus.core.jdbc;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class QueryTest {

  @Test
  public void test() {
    Query query = new Query("SELECT * FROM member", Arrays.asList(1, 2, 3));
    assertEquals("SELECT * FROM member", query.getSQL());
    assertEquals(3, query.getParameters().size());
    assertEquals(1, query.getParameters().get(0));
    assertEquals(2, query.getParameters().get(1));
    assertEquals(3, query.getParameters().get(2));
  }

}
