package cloud.xcan.angus.cache.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class CacheManagementControllerTest {

  private MockMvc mockMvc;
  private IDistributedCache cache;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    cache = Mockito.mock(IDistributedCache.class);
    CacheManagementController controller = new CacheManagementController(cache);
    // Standalone MockMvc: use Spring's default handler so ResponseStatusException → HTTP status.
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new ResponseEntityExceptionHandler() {
        })
        .build();
  }

  // ── GET /{key} ───────────────────────────────────────────────────────────────

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
  void testGet_blankKey_returnsBadRequest() throws Exception {
    // URI template expands and decodes the variable; raw "/.../%20" can bind as literal "%20".
    mockMvc.perform(get("/api/v1/cache/{key}", "   "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGet_keyTooLong_returnsBadRequest() throws Exception {
    String longKey = "k".repeat(257);
    mockMvc.perform(get("/api/v1/cache/{key}", longKey))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /{key} ───────────────────────────────────────────────────────────────

  @Test
  void testSet() throws Exception {
    String body = objectMapper.writeValueAsString(
        new CacheManagementController.SetCacheRequest("v1", null));
    mockMvc.perform(put("/api/v1/cache/key1").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
  }

  @Test
  void testSet_withTtl() throws Exception {
    String body = objectMapper.writeValueAsString(
        new CacheManagementController.SetCacheRequest("v_ttl", 60L));
    mockMvc.perform(
            put("/api/v1/cache/ttlkey").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
    verify(cache).set("ttlkey", "v_ttl", 60L);
  }

  // ── DELETE /{key} ─────────────────────────────────────────────────────────────

  @Test
  void testDelete() throws Exception {
    when(cache.delete("delkey")).thenReturn(true);
    mockMvc.perform(delete("/api/v1/cache/delkey"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
    verify(cache).delete("delkey");
  }

  // ── GET /{key}/exists ─────────────────────────────────────────────────────────

  @Test
  void testExists_found() throws Exception {
    when(cache.exists("exkey")).thenReturn(true);
    mockMvc.perform(get("/api/v1/cache/exkey/exists"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.exists").value(true));
  }

  @Test
  void testExists_notFound() throws Exception {
    when(cache.exists("missing")).thenReturn(false);
    mockMvc.perform(get("/api/v1/cache/missing/exists"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.exists").value(false));
  }

  // ── GET /{key}/ttl ────────────────────────────────────────────────────────────

  @Test
  void testTtl() throws Exception {
    when(cache.getTTL("ttlkey")).thenReturn(42L);
    mockMvc.perform(get("/api/v1/cache/ttlkey/ttl"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.ttl").value(42));
  }

  // ── POST /{key}/expire ────────────────────────────────────────────────────────

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

  @Test
  void testExpire_keyNotFound() throws Exception {
    when(cache.expire(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenReturn(false);
    String body = objectMapper.writeValueAsString(new CacheManagementController.ExpireRequest(60));
    mockMvc.perform(
            post("/api/v1/cache/ghost/expire").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("E1"))
        .andExpect(jsonPath("$.message").value("Key not found"));
  }

  // ── POST /clear ───────────────────────────────────────────────────────────────

  @Test
  void testClear() throws Exception {
    mockMvc.perform(post("/api/v1/cache/clear"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
    verify(cache).clear();
  }

  // ── POST /cleanup ─────────────────────────────────────────────────────────────

  @Test
  void testCleanup() throws Exception {
    when(cache.cleanupExpiredEntries()).thenReturn(5);
    mockMvc.perform(post("/api/v1/cache/cleanup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.deletedCount").value(5));
  }

  // ── GET /stats ─────────────────────────────────────────────────────────────────

  @Test
  void testStats() throws Exception {
    CacheStats stats = CacheStats.builder()
        .totalEntries(10).activeEntries(8).expiredEntries(2)
        .memorySize(5).databaseSize(10).hits(100).misses(10).hitRate(0.91)
        .build();
    when(cache.getStats()).thenReturn(stats);
    mockMvc.perform(get("/api/v1/cache/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.totalEntries").value(10))
        .andExpect(jsonPath("$.data.activeEntries").value(8));
  }
}

