package cloud.xcan.angus.job.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.enums.JobType;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.UpdateJobRequest;
import cloud.xcan.angus.job.service.JobManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

@WebMvcTest(controllers = JobController.class)
class JobControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JobManagementService jobManagementService;

  // ---------------------------------------------------------------------------
  // POST /api/v1/jobs
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("POST /api/v1/jobs - creates job and returns 200")
  void createJob_success() throws Exception {
    CreateJobRequest req = validRequest();
    ScheduledJob saved = savedJob(1L, req.getJobName());
    when(jobManagementService.createJob(any())).thenReturn(saved);

    mockMvc.perform(post("/api/v1/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.jobName").value("DailyReport"));
  }

  @Test
  @DisplayName("POST /api/v1/jobs - returns 400 when beanName contains invalid chars")
  void createJob_invalidBeanName() throws Exception {
    CreateJobRequest req = validRequest();
    req.setBeanName("../../../evil");

    mockMvc.perform(post("/api/v1/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/v1/jobs - propagates IllegalArgumentException (no @ControllerAdvice)")
  void createJob_illegalArgumentFromService() throws Exception {
    CreateJobRequest req = validRequest();
    when(jobManagementService.createJob(any()))
        .thenThrow(new IllegalArgumentException("invalid cron"));

    assertRootCause(
        mockMvc,
        post("/api/v1/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)),
        IllegalArgumentException.class);
  }

  @Test
  @DisplayName("POST /api/v1/jobs - returns 400 when required fields are missing")
  void createJob_missingFields() throws Exception {
    CreateJobRequest req = new CreateJobRequest(); // all null

    mockMvc.perform(post("/api/v1/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------------------------------------------------------
  // GET /api/v1/jobs
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("GET /api/v1/jobs - returns paged list")
  void listJobs() throws Exception {
    when(jobManagementService.listJobs(any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(savedJob(1L, "Job1"))));

    mockMvc.perform(get("/api/v1/jobs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].jobName").value("Job1"));
  }

  // ---------------------------------------------------------------------------
  // GET /api/v1/jobs/{id}
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("GET /api/v1/jobs/1 - returns job")
  void getJob_success() throws Exception {
    when(jobManagementService.getJob(1L)).thenReturn(savedJob(1L, "One"));

    mockMvc.perform(get("/api/v1/jobs/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.jobName").value("One"));
  }

  @Test
  @DisplayName("GET /api/v1/jobs/999 - propagates EntityNotFoundException (no @ControllerAdvice)")
  void getJob_notFound() {
    when(jobManagementService.getJob(999L))
        .thenThrow(new EntityNotFoundException("Job not found: 999"));

    assertRootCause(mockMvc, get("/api/v1/jobs/999"), EntityNotFoundException.class);
  }

  // ---------------------------------------------------------------------------
  // POST /{jobId}/trigger
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("POST /api/v1/jobs/1/trigger - propagates IllegalStateException (no @ControllerAdvice)")
  void triggerJob_running() {
    org.mockito.Mockito.doThrow(
            new IllegalStateException("Job 1 cannot be triggered in status RUNNING"))
        .when(jobManagementService).triggerJob(1L);

    assertRootCause(mockMvc, post("/api/v1/jobs/1/trigger"), IllegalStateException.class);
  }

  // ---------------------------------------------------------------------------
  // DELETE /api/v1/jobs/{id}
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("PUT /api/v1/jobs/1 - updates job")
  void updateJob_success() throws Exception {
    UpdateJobRequest req = new UpdateJobRequest();
    req.setJobName("U");
    req.setCronExpression("0 0 * * * *");
    req.setDescription("d");
    when(jobManagementService.updateJob(eq(1L), any()))
        .thenReturn(savedJob(1L, "U"));

    mockMvc.perform(put("/api/v1/jobs/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.jobName").value("U"));
  }

  @Test
  @DisplayName("POST pause / resume - 200 wrapped success")
  void pauseAndResume() throws Exception {
    mockMvc.perform(post("/api/v1/jobs/1/pause"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
    mockMvc.perform(post("/api/v1/jobs/1/resume"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("S"));
    verify(jobManagementService).pauseJob(1L);
    verify(jobManagementService).resumeJob(1L);
  }

  @Test
  @DisplayName("GET executions and statistics")
  void executionsAndStatistics() throws Exception {
    when(jobManagementService.getJobExecutionHistory(eq(1L), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));
    when(jobManagementService.getJobStatistics(1L))
        .thenReturn(Map.of("totalExecutions", 0));

    mockMvc.perform(get("/api/v1/jobs/1/executions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
    mockMvc.perform(get("/api/v1/jobs/1/statistics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalExecutions").value(0));
  }

  @Test
  @DisplayName("DELETE /api/v1/jobs/1 - returns 204 No Content")
  void deleteJob_success() throws Exception {
    mockMvc.perform(delete("/api/v1/jobs/1"))
        .andExpect(status().isNoContent());
    verify(jobManagementService).deleteJob(1L);
  }

  // ---------------------------------------------------------------------------
  // Unhandled service exceptions
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("Unexpected RuntimeException propagates via ServletException (no @ControllerAdvice)")
  void unexpectedException_returns500Generic() {
    when(jobManagementService.listJobs(any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("DB connection string: jdbc:mysql://secret"));

    assertRootCause(mockMvc, get("/api/v1/jobs"), RuntimeException.class);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Without a {@code @ControllerAdvice}, DispatcherServlet lets handler exceptions propagate;
   * {@link MockMvc#perform} then throws {@link ServletException} wrapping the original error.
   */
  private static void assertRootCause(
      MockMvc mockMvc, RequestBuilder request, Class<? extends Throwable> expectedRoot) {
    ServletException ex = assertThrows(ServletException.class, () -> mockMvc.perform(request));
    Throwable cur = ex;
    while (cur.getCause() != null) {
      cur = cur.getCause();
    }
    assertInstanceOf(expectedRoot, cur);
  }

  private static CreateJobRequest validRequest() {
    CreateJobRequest req = new CreateJobRequest();
    req.setJobName("DailyReport");
    req.setJobGroup("Reports");
    req.setCronExpression("0 0 1 * * *");
    req.setBeanName("dailyReportExecutor");
    req.setJobType(JobType.SIMPLE);
    return req;
  }

  private static ScheduledJob savedJob(Long id, String name) {
    ScheduledJob j = new ScheduledJob();
    j.setId(id);
    j.setJobName(name);
    j.setJobGroup("Reports");
    j.setStatus(JobStatus.READY);
    j.setJobType(JobType.SIMPLE);
    j.setBeanName("exec");
    j.setCronExpression("0 0 1 * * *");
    return j;
  }
}
