package cloud.xcan.angus.job.web;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.UpdateJobRequest;
import cloud.xcan.angus.job.service.JobManagementService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for managing scheduled jobs.
 *
 * <p>This controller owns no repository references — all data access is
 * delegated to {@link JobManagementService}, which enforces business rules and audit logic (P1
 * fix).
 *
 * <p>Input is validated via {@code @Valid} on DTO parameters.  JPA entities
 * are never accepted directly as request bodies to prevent mass-assignment.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

  private final JobManagementService jobManagementService;

  // ---------------------------------------------------------------------------
  // CRUD
  // ---------------------------------------------------------------------------

  @PostMapping
  public ResponseEntity<ScheduledJob> createJob(@Valid @RequestBody CreateJobRequest request) {
    return ResponseEntity.ok(jobManagementService.createJob(request));
  }

  @GetMapping
  public ResponseEntity<Page<ScheduledJob>> listJobs(
      @PageableDefault(size = 20, sort = "createTime", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ResponseEntity.ok(jobManagementService.listJobs(pageable));
  }

  @GetMapping("/{jobId}")
  public ResponseEntity<ScheduledJob> getJob(@PathVariable Long jobId) {
    return ResponseEntity.ok(jobManagementService.getJob(jobId));
  }

  @PutMapping("/{jobId}")
  public ResponseEntity<ScheduledJob> updateJob(
      @PathVariable Long jobId,
      @Valid @RequestBody UpdateJobRequest request) {
    return ResponseEntity.ok(jobManagementService.updateJob(jobId, request));
  }

  @DeleteMapping("/{jobId}")
  public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
    jobManagementService.deleteJob(jobId);
    return ResponseEntity.noContent().build();
  }

  // ---------------------------------------------------------------------------
  // Operational controls
  // ---------------------------------------------------------------------------

  @PostMapping("/{jobId}/pause")
  public ResponseEntity<Void> pauseJob(@PathVariable Long jobId) {
    jobManagementService.pauseJob(jobId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{jobId}/resume")
  public ResponseEntity<Void> resumeJob(@PathVariable Long jobId) {
    jobManagementService.resumeJob(jobId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{jobId}/trigger")
  public ResponseEntity<Void> triggerJob(@PathVariable Long jobId) {
    jobManagementService.triggerJob(jobId);
    return ResponseEntity.noContent().build();
  }

  // ---------------------------------------------------------------------------
  // History and statistics (paginated)
  // ---------------------------------------------------------------------------

  @GetMapping("/{jobId}/executions")
  public ResponseEntity<Page<JobExecutionLog>> getExecutionHistory(
      @PathVariable Long jobId,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(jobManagementService.getJobExecutionHistory(jobId, pageable));
  }

  @GetMapping("/{jobId}/statistics")
  public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Long jobId) {
    return ResponseEntity.ok(jobManagementService.getJobStatistics(jobId));
  }
}
