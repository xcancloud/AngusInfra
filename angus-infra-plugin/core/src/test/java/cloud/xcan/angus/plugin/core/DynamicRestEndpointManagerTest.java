package cloud.xcan.angus.plugin.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.api.PluginController;
import cloud.xcan.angus.plugin.api.RestfulPlugin;
import cloud.xcan.angus.plugin.core.DynamicRestEndpointManager.EndpointInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class DynamicRestEndpointManagerTest {

  private RequestMappingHandlerMapping handlerMapping;
  private ConfigurableApplicationContext applicationContext;
  private DynamicRestEndpointManager manager;

  @BeforeEach
  void setUp() {
    handlerMapping = mock(RequestMappingHandlerMapping.class);
    applicationContext = mock(ConfigurableApplicationContext.class);
    AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
    when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
    manager = new DynamicRestEndpointManager(handlerMapping, applicationContext);
  }

  @Test
  void registerWithEmptyControllersSucceeds() {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenReturn(Collections.emptyList());
    PluginContext ctx = mock(PluginContext.class);
    assertTrue(manager.registerPlugin("e", plugin, ctx));
    assertTrue(manager.getPluginEndpoints("e").isEmpty());
  }

  @Test
  void registerMapsGetAndPost() {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenReturn(List.of(GetCtrl.class, PostCtrl.class));
    when(plugin.getApiPrefix()).thenReturn("/pfx");
    PluginContext ctx = mock(PluginContext.class);
    assertTrue(manager.registerPlugin("m", plugin, ctx));
    verify(handlerMapping, org.mockito.Mockito.atLeast(2)).registerMapping(
        any(RequestMappingInfo.class), any(), any());
    List<EndpointInfo> endpoints = manager.getPluginEndpoints("m");
    assertEquals(2, endpoints.size());
  }

  @Test
  void registerFailureRollsBack() {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenThrow(new RuntimeException("fail"));
    assertFalse(manager.registerPlugin("bad", plugin, mock(PluginContext.class)));
  }

  @Test
  void unregisterInvokesHandlerMapping() {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenReturn(List.of(GetCtrl.class));
    PluginContext ctx = mock(PluginContext.class);
    manager.registerPlugin("u", plugin, ctx);
    assertTrue(manager.unregisterPlugin("u"));
    verify(handlerMapping).unregisterMapping(any(RequestMappingInfo.class));
  }

  @Test
  void getAllPluginEndpointsAggregates() {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenReturn(List.of(GetCtrl.class));
    manager.registerPlugin("a", plugin, mock(PluginContext.class));
    Map<String, List<EndpointInfo>> all = manager.getAllPluginEndpoints();
    assertEquals(1, all.size());
    assertTrue(all.containsKey("a"));
  }

  @Test
  void endpointInfoBuilderAndOf() {
    EndpointInfo i = EndpointInfo.builder().path("/p").methods(List.of("GET")).build();
    assertEquals("/p", i.getPath());
    EndpointInfo j = EndpointInfo.of("/x", List.of("POST"));
    assertEquals("/x", j.getPath());
    EndpointInfo k = new EndpointInfo();
    k.setPath("/z");
    assertEquals("/z", k.getPath());
    assertNotNull(EndpointInfo.builder());
  }

  @Test
  void pluginControllerCleanupOnUnregister() throws Exception {
    RestfulPlugin plugin = mock(RestfulPlugin.class);
    when(plugin.getControllerClasses()).thenReturn(List.of(CloseableCtrl.class));
    manager.registerPlugin("c", plugin, mock(PluginContext.class));
    manager.unregisterPlugin("c");
  }

  @RestController
  @RequestMapping("/g")
  public static class GetCtrl extends PluginController {

    @GetMapping("/a")
    public String a() {
      return "a";
    }
  }

  @RestController
  public static class PostCtrl {

    @PostMapping("/b")
    public String b() {
      return "b";
    }
  }

  @RestController
  public static class CloseableCtrl extends PluginController implements AutoCloseable {

    @GetMapping("/c")
    public String c() {
      return "c";
    }

    @Override
    public void close() {
    }
  }
}
