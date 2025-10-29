package cloud.xcan.angus.queue.starter.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.queue.core.model.MessageData;
import cloud.xcan.angus.queue.core.service.QueueService;
import cloud.xcan.angus.queue.starter.autoconfigure.QueueProperties;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class QueueControllerTest {

  private MockMvc mvc;
  private QueueService queueService;
  private QueueProperties properties;

  @BeforeEach
  void setUp() {
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    System.setProperty("logging.system", "none");

    // Use CALLS_REAL_METHODS so interface default methods delegate to the concrete overloads we stub
    queueService = Mockito.mock(QueueService.class, Mockito.CALLS_REAL_METHODS);
    properties = new QueueProperties();
    properties.setPartitions(4);
    properties.setPollBatch(10);
    properties.setLeaseSeconds(30);

    QueueController controller = new QueueController(queueService, properties);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void sendOk() throws Exception {
    when(queueService.send(
        anyString(),
        nullable(String.class),
        anyString(),
        nullable(String.class),
        anyInt(),
        any(Instant.class),
        nullable(String.class),
        anyInt(),
        anyInt()
    )).thenReturn(123L);
    mvc.perform(post("/api/v1/queue/send")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"topic\":\"t1\",\"payload\":\"p\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id", is(123)));
  }

  @Test
  void pollOk() throws Exception {
    when(queueService.lease(anyString(), anyList(), anyString(), anyInt(), anyInt()))
        .thenReturn(1);
    when(queueService.listLeasedByOwner(anyString(), anyInt())).thenReturn(
        List.of(new MessageData()));
    mvc.perform(post("/api/v1/queue/poll")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"topic\":\"t1\",\"owner\":\"o\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  void ackOk() throws Exception {
    Mockito.when(queueService.ack(anyList())).thenReturn(2);
    mvc.perform(post("/api/v1/queue/ack")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"ids\":[1,2]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.acked", is(2)));
  }

  @Test
  void nackOk() throws Exception {
    Mockito.when(queueService.nack(anyList(), anyInt())).thenReturn(2);
    mvc.perform(post("/api/v1/queue/nack")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"ids\":[1,2],\"backoffSeconds\":5}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nacked", is(2)));
  }
}
