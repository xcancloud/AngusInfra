package cloud.xcan.angus.plugin.management;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.plugin.model.PluginInfo;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class PluginManagementControllerTest {

  private MockMvc mockMvc;
  private PluginManagementService service;

  @BeforeEach
  void setUp() {
    service = Mockito.mock(PluginManagementService.class);
    PluginManagementController controller = new PluginManagementController(service);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void testList() throws Exception {
    PluginInfo a = new PluginInfo();
    a.setId("a");
    PluginInfo b = new PluginInfo();
    b.setId("b");
    when(service.listPlugins()).thenReturn(Arrays.asList(a, b));

    mockMvc.perform(get("/api/v1/plugin-management/list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  void testGetPluginNotFound() throws Exception {
    when(service.getPlugin("nope")).thenReturn(null);
    mockMvc.perform(get("/api/v1/plugin-management/nope"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E2"));
  }

  @Test
  void testStats() throws Exception {
    PluginInfo a = new PluginInfo();
    a.setId("a");
    a.setEndpointCount(1);
    when(service.stats()).thenReturn(new PluginStats(1, 1, 1));
    mockMvc.perform(get("/api/v1/plugin-management/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.totalPlugins").value(1));
  }

  @Test
  void testRemoveOk() throws Exception {
    mockMvc.perform(delete("/api/v1/plugin-management/p1?removeFromStore=true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
  }
}
