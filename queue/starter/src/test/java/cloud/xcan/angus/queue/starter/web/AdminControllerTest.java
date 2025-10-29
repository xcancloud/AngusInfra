package cloud.xcan.angus.queue.starter.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.queue.starter.service.AdminService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminControllerTest {

  private MockMvc mvc;
  private AdminService adminService;

  @BeforeEach
  void setup() {
    // Disable Boot logging early just in case
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    System.setProperty("logging.system", "none");
    adminService = Mockito.mock(AdminService.class);
    AdminController controller = new AdminController(adminService);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void statsOk() throws Exception {
    Mockito.when(adminService.topicStats("t1")).thenReturn(Map.of("dlqCount", 3));
    mvc.perform(get("/api/v1/queue/admin/stats").param("topic", "t1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.dlqCount", is(3)));
  }

  @Test
  void reclaimOk() throws Exception {
    Mockito.when(adminService.reclaimExpired(500)).thenReturn(10);
    mvc.perform(post("/api/v1/queue/admin/reclaim").param("limit", "500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reclaimed", is(10)));
  }

  @Test
  void purgeDoneOk() throws Exception {
    Mockito.when(adminService.purgeDone(eq("t1"), any(Instant.class))).thenReturn(9);
    mvc.perform(delete("/api/v1/queue/admin/purge/done").param("topic", "t1")
            .param("before", "2025-10-28T00:00:00Z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.purged", is(9)));
  }

  @Test
  void purgeDlqOk() throws Exception {
    Mockito.when(adminService.purgeDeadLetters("t1")).thenReturn(5);
    mvc.perform(delete("/api/v1/queue/admin/purge/dlq").param("topic", "t1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.purged", is(5)));
  }

  @Test
  void replayDlqOk() throws Exception {
    Mockito.when(adminService.replayFromDeadLetter("t1", 3)).thenReturn(3);
    mvc.perform(post("/api/v1/queue/admin/dlq/replay").param("topic", "t1").param("limit", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.replayed", is(3)));
  }
}
