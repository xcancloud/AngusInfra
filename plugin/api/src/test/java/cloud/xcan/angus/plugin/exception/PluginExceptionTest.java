package cloud.xcan.angus.plugin.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PluginExceptionTest {

  @Test
  void constructors() {
    PluginException a = new PluginException();
    assertNull(a.getMessage());

    PluginException b = new PluginException("m");
    assertEquals("m", b.getMessage());

    Throwable cause = new RuntimeException("c");
    PluginException c = new PluginException("m2", cause);
    assertEquals("m2", c.getMessage());
    assertEquals(cause, c.getCause());
  }
}
