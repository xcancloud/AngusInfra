package cloud.xcan.angus.plugin.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.model.PluginDescriptor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

class DefaultPluginContextTest {

  @Test
  void configurationDataDirLoggingServices(@TempDir Path tmp) {
    ApplicationContext app = mock(ApplicationContext.class);
    when(app.getBean(String.class)).thenReturn("bean");
    PluginDescriptor d = new PluginDescriptor();
    d.setId("pid");
    Map<String, Object> cfg = new HashMap<>();
    cfg.put("k", "v");
    PluginProperties props = PluginProperties.forTesting(tmp.resolve("plugins").toString(),
        tmp.toString());
    DefaultPluginContext ctx = new DefaultPluginContext(app, d, cfg, props);

    assertSame(app, ctx.getApplicationContext());
    assertEquals("v", ctx.getConfiguration().get("k"));
    assertEquals(tmp.resolve("pid"), ctx.getDataDirectory());

    ctx.log(null, "x");
    ctx.log("INFO", null);
    ctx.log("info", "hello");
    ctx.log("DEBUG", "d");
    ctx.log("WARN", "w");
    ctx.log("ERROR", "e");
    ctx.log("OTHER", "o");

    ctx.log("INFO", "m", new RuntimeException("x"));
    ctx.log(null, "m", new RuntimeException());

    ctx.registerService("s", "impl");
    assertEquals("impl", ctx.getService("s", String.class));
    assertNull(ctx.getService("missing", String.class));

    assertEquals("pid", ctx.getPluginId());
    assertEquals("bean", ctx.getBean(String.class));
  }

  @Test
  void getEnvironmentDelegates() {
    ApplicationContext app = mock(ApplicationContext.class);
    PluginDescriptor d = new PluginDescriptor();
    d.setId("p");
    DefaultPluginContext ctx = new DefaultPluginContext(app, d, new HashMap<>(),
        new PluginProperties());
    assertNotNull(ctx.getEnvironment("PATH"));
    assertEquals("d", ctx.getEnvironment("__NO_SUCH_ENV___", "d"));
  }
}
