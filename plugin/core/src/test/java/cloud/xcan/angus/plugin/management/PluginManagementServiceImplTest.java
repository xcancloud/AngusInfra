package cloud.xcan.angus.plugin.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.plugin.api.PluginManager;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PluginManagementServiceImplTest {

  private PluginManager pluginManager;
  private PluginManagementServiceImpl service;

  @BeforeEach
  void setUp() {
    pluginManager = Mockito.mock(PluginManager.class);
    service = new PluginManagementServiceImpl(pluginManager);
  }

  @Test
  void testInitializeDelegates() {
    service.initialize();
    verify(pluginManager, times(1)).initialize();
  }

  @Test
  void testReloadDelegates() {
    service.reloadAll();
    verify(pluginManager, times(1)).loadAllPlugins();
  }

  @Test
  void testInstallSuccessReturnsInfo() throws PluginException {
    byte[] data = new byte[]{1, 2, 3};
    when(pluginManager.installPlugin("p1", data)).thenReturn(true);
    PluginInfo info = new PluginInfo();
    info.setId("p1");
    when(pluginManager.getPluginInfo("p1")).thenReturn(info);

    PluginInfo result = service.install("p1", data);
    assertNotNull(result);
    assertEquals("p1", result.getId());
  }

  @Test
  void testInstallFailureReturnsNull() throws PluginException {
    when(pluginManager.installPlugin("p2", new byte[]{4})).thenReturn(false);
    PluginInfo result = service.install("p2", new byte[]{4});
    assertNull(result);
  }

  @Test
  void testRemoveThrowsWhenPluginManagerFails() throws PluginException {
    when(pluginManager.removePlugin("p1", true)).thenReturn(false);
    assertThrows(PluginException.class, () -> service.remove("p1", true));
  }

  @Test
  void testListAndGetDelegation() {
    PluginInfo a = new PluginInfo();
    a.setId("a");
    PluginInfo b = new PluginInfo();
    b.setId("b");
    List<PluginInfo> list = Arrays.asList(a, b);
    when(pluginManager.getAllPlugins()).thenReturn(list);
    when(pluginManager.getPluginInfo("a")).thenReturn(a);

    List<PluginInfo> res = service.listPlugins();
    assertEquals(2, res.size());
    PluginInfo got = service.getPlugin("a");
    assertEquals("a", got.getId());
  }

  @Test
  void testStats() {
    PluginInfo a = new PluginInfo();
    a.setId("a");
    a.setEndpointCount(2);
    PluginInfo b = new PluginInfo();
    b.setId("b");
    b.setEndpointCount(0);
    when(pluginManager.getAllPlugins()).thenReturn(Arrays.asList(a, b));

    PluginStats stats = service.stats();
    assertEquals(2, stats.getTotalPlugins());
    assertEquals(1, stats.getActivePlugins());
    assertEquals(2, stats.getRestEndpoints());
  }
}

