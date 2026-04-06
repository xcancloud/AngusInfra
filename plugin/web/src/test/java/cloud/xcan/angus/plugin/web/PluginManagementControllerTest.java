package cloud.xcan.angus.plugin.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.management.PluginManagementService;
import cloud.xcan.angus.plugin.management.PluginStats;
import cloud.xcan.angus.plugin.model.PluginInfo;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class PluginManagementControllerTest {

  private MockMvc mockMvc;
  private PluginManagementService service;
  private PluginProperties pluginProperties;

  @BeforeEach
  void setUp() {
    service = Mockito.mock(PluginManagementService.class);
    pluginProperties = Mockito.mock(PluginProperties.class);
    when(pluginProperties.getMaxUploadSize()).thenReturn(1024L);
    PluginManagementController controller = new PluginManagementController(service,
        pluginProperties);
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
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetPluginOk() throws Exception {
    PluginInfo info = new PluginInfo();
    info.setId("p1");
    when(service.getPlugin("p1")).thenReturn(info);
    mockMvc.perform(get("/api/v1/plugin-management/p1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.id").value("p1"));
  }

  @Test
  void testStats() throws Exception {
    when(service.stats()).thenReturn(new PluginStats(1, 1, 1));
    mockMvc.perform(get("/api/v1/plugin-management/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.totalPlugins").value(1));
  }

  @Test
  void testRemoveOk() throws Exception {
    mockMvc.perform(delete("/api/v1/plugin-management/p1?removeFromStore=true"))
        .andExpect(status().isNoContent());
  }

  @Test
  void testRemovePluginExceptionReturnsError() throws Exception {
    doThrow(new PluginException("x")).when(service).remove(eq("bad"), anyBoolean());
    mockMvc.perform(delete("/api/v1/plugin-management/bad"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void testInstallTooLarge() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "p.jar", null, new byte[2048]);
    mockMvc.perform(multipart("/api/v1/plugin-management/install")
            .file(file)
            .param("pluginId", "pid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E2"));
  }

  @Test
  void testInstallSuccess() throws Exception {
    PluginInfo info = new PluginInfo();
    info.setId("pid");
    when(service.install(eq("pid"), any())).thenReturn(info);
    MockMultipartFile file = new MockMultipartFile("file", "p.jar", null, new byte[]{1, 2});
    mockMvc.perform(multipart("/api/v1/plugin-management/install")
            .file(file)
            .param("pluginId", "pid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.id").value("pid"));
  }

  @Test
  void testInstallUnexpectedException() throws Exception {
    when(service.install(any(), any())).thenThrow(new RuntimeException("boom"));
    MockMultipartFile file = new MockMultipartFile("file", "p.jar", null, new byte[]{1});
    mockMvc.perform(multipart("/api/v1/plugin-management/install")
            .file(file)
            .param("pluginId", "pid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E2"));
  }

  @Test
  void testInstallRejectsNonJarFile() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "plugin.txt", null, new byte[]{1, 2});
    mockMvc.perform(multipart("/api/v1/plugin-management/install")
            .file(file)
            .param("pluginId", "pid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E2"));
  }

  @Test
  void testInstallRejectsBlankPluginId() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "p.jar", null, new byte[]{1, 2});
    mockMvc.perform(multipart("/api/v1/plugin-management/install")
            .file(file)
            .param("pluginId", "  "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E2"));
  }
}
