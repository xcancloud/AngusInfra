package cloud.xcan.angus.job.web;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.UpdateJobRequest;
import cloud.xcan.angus.job.service.JobManagementService;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for managing scheduled jobs.
 *
 * <p>This controller owns no jpa references — all data access is
 * delegated to {@link JobManagementService}, which enforces business rules and audit logic (P1
 * fix).
 *
 * <p>Input is validated via {@code @Valid} on DTO parameters.  JPA entities
 * are never accepted directly as request bodies to prevent mass-assignment.
 */
@Slf4j
@Tag(name = "Job", description = "APIs for managing distributed scheduled jobs")
@PreAuthorize("@PPS.isCloudTenantSecurity() && @PPS.isSysAdmin()")
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

  private final JobManagementService jobManagementService;

  @Operation(operationId = "createJob", summary = "Create a scheduled job",
      description = "Create a new scheduled job with the given configuration. The job will be registered but not started until it is explicitly triggered or its schedule fires.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job created successfully")
      })
  @PostMapping
  public ApiLocaleResult<ScheduledJob> createJob(@Valid @RequestBody CreateJobRequest request) {
    return ApiLocaleResult.success(jobManagementService.createJob(request));
  }

  @Operation(operationId = "listJobs", summary = "List all scheduled jobs",
      description = "Return a paginated list of all scheduled jobs, sorted by creation time in descending order by default. "
          + "Supports keyword search (matches jobName, jobGroup, beanName) and status filtering.")
  @GetMapping
  public ApiLocaleResult<Page<ScheduledJob>> listJobs(
      @Parameter(description = "Search keyword (matches jobName, jobGroup, beanName)")
      @RequestParam(required = false) String keyword,
      @Parameter(description = "Filter by job status")
      @RequestParam(required = false) JobStatus status,
      @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ApiLocaleResult.success(jobManagementService.listJobs(keyword, status, pageable));
  }

  @Operation(operationId = "getJob", summary = "Get job details",
      description = "Retrieve the full details of a scheduled job by its ID.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job found"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @GetMapping("/{jobId}")
  public ApiLocaleResult<ScheduledJob> getJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    return ApiLocaleResult.success(jobManagementService.getJob(jobId));
  }

  @Operation(operationId = "updateJob", summary = "Update a scheduled job",
      description = "Update the configuration of an existing scheduled job. Only the fields provided in the request body will be modified.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job updated successfully"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @PutMapping("/{jobId}")
  public ApiLocaleResult<ScheduledJob> updateJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId,
      @Valid @RequestBody UpdateJobRequest request) {
    return ApiLocaleResult.success(jobManagementService.updateJob(jobId, request));
  }

  @Operation(operationId = "deleteJob", summary = "Delete a scheduled job",
      description = "Delete a scheduled job and all associated execution history. This operation is irreversible.",
      responses = {
          @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @DeleteMapping("/{jobId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    jobManagementService.deleteJob(jobId);
  }

  @Operation(operationId = "pauseJob", summary = "Pause a scheduled job",
      description = "Pause a running or scheduled job. The job will not be executed until it is resumed.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job paused successfully"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @PostMapping("/{jobId}/pause")
  public ApiLocaleResult<Void> pauseJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    jobManagementService.pauseJob(jobId);
    return new ApiLocaleResult<>();
  }

  @Operation(operationId = "resumeJob", summary = "Resume a paused job",
      description = "Resume a previously paused job so it can be executed according to its schedule again.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job resumed successfully"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @PostMapping("/{jobId}/resume")
  public ApiLocaleResult<Void> resumeJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    jobManagementService.resumeJob(jobId);
    return new ApiLocaleResult<>();
  }

  @Operation(operationId = "triggerJob", summary = "Trigger immediate job execution",
      description = "Trigger an immediate one-time execution of the specified job, regardless of its current schedule.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Job triggered successfully"),
          @ApiResponse(responseCode = "404", description = "Job not found")
      })
  @PostMapping("/{jobId}/trigger")
  public ApiLocaleResult<Void> triggerJob(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    jobManagementService.triggerJob(jobId);
    return new ApiLocaleResult<>();
  }

  @Operation(operationId = "getJobExecutionHistory", summary = "Get job execution history",
      description = "Return a paginated list of execution logs for the specified job, ordered by execution time.")
  @GetMapping("/{jobId}/executions")
  public ApiLocaleResult<Page<JobExecutionLog>> getExecutionHistory(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId,
      @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ApiLocaleResult.success(
        jobManagementService.getJobExecutionHistory(jobId, pageable));
  }

  @Operation(operationId = "getJobStatistics", summary = "Get job execution statistics",
      description = "Return aggregated statistics for the specified job, including execution counts, success/failure rates and average duration.")
  @GetMapping("/{jobId}/statistics")
  public ApiLocaleResult<Map<String, Object>> getStatistics(
      @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
    return ApiLocaleResult.success(jobManagementService.getJobStatistics(jobId));
  }
}
