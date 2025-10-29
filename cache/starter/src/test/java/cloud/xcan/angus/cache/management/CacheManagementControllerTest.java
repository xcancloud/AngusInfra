package cloud.xcan.angus.cache.management;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.cache.web.CacheManagementController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class CacheManagementControllerTest {

  private MockMvc mockMvc;
  private IDistributedCache cache;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    cache = Mockito.mock(IDistributedCache.class);
    CacheManagementController controller = new CacheManagementController(cache);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void testGetFound() throws Exception {
    when(cache.get("foo")).thenReturn(Optional.of("bar"));

    mockMvc.perform(get("/api/v1/cache/foo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.value").value("bar"));
  }

  @Test
  void testGetNotFound() throws Exception {
    when(cache.get("nope")).thenReturn(Optional.empty());
    mockMvc.perform(get("/api/v1/cache/nope"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E1"))
        .andExpect(jsonPath("$.message").value("Key not found"));
  }

  @Test
  void testSet() throws Exception {
    String body = objectMapper.writeValueAsString(new CacheManagementController.SetCacheRequest("v1", null));
    mockMvc.perform(put("/api/v1/cache/key1").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
  }

  @Test
  void testExpire() throws Exception {
    when(cache.expire(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenReturn(true);
    String body = objectMapper.writeValueAsString(new CacheManagementController.ExpireRequest(60));
    mockMvc.perform(
            post("/api/v1/cache/key1/expire").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.success").value(true));
  }
}
