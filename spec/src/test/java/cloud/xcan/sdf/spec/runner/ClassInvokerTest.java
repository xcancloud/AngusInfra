package cloud.xcan.sdf.spec.runner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClassInvokerTest {

  /**
   * Test of getDir method, of class NewDriver.
   */
  @Test
  public void testGetDir() {
    String result = ClassInvoker.getJarLocation();
    System.out.println(result);
    assertTrue(result.length() > 0);
  }

  /**
   * Test of invoke method, of class NewDriver.
   */
  @Test
  public void testMain() throws Throwable {
    Object result = ClassInvoker.invoke(ClassInvoker.class.getName(), "getJarLocation", null);
    System.out.println(result);
    assertNotNull(result);
    assertTrue(result.toString().length() > 0);
  }
}