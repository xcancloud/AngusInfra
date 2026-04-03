package cloud.xcan.angus.job.service;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.jpa.JobShardRepository;
import cloud.xcan.angus.job.jpa.ScheduledJobRepository;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.UpdateJobRequest;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for CRUD management and operational control of scheduled jobs.
 *
 * <p>All database access is routed through this service; the REST controller
 * does not hold any repository references.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobManagementService {

  private final ScheduledJobRepository jobRepository;
  private final JobExecutionLogRepository executionLogRepository;
  private final JobShardRepository shardRepository;

  /**
   * Registers a new job definition.
   *
   * @param request validated creation request
   * @return the persisted job entity
   * @throws IllegalArgumentException if the cron expression is invalid
   */
  @Transactional
  public ScheduledJob createJob(CreateJobRequest request) {
    LocalDateTime now = LocalDateTime.now();

    ScheduledJob job = new ScheduledJob();
    job.setJobName(request.getJobName());
    job.setJobGroup(request.getJobGroup());
    job.setCronExpression(request.getCronExpression());
    job.setBeanName(request.getBeanName());
    job.setJobType(request.getJobType());
    job.setShardingCount(request.getShardingCount() != null ? request.getShardingCount() : 1);
    job.setShardingParameter(request.getShardingParameter());
    job.setMaxRetryCount(request.getMaxRetryCount() != null ? request.getMaxRetryCount() : 3);
    job.setRetryCount(0);
    job.setDescription(request.getDescription());
    // 0 表示全局默认（7天），-1 表示永久保留；原样存储，由清理 job 按规则处理
    job.setLogRetentionDays(request.getLogRetentionDays() != null ? request.getLogRetentionDays() : 0);
    job.setStatus(JobStatus.READY);
    job.setCreateTime(now);
    job.setUpdateTime(now);
    job.setNextExecuteTime(calcNextExecuteTime(request.getCronExpression()));

    try {
      return jobRepository.save(job);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalStateException(
          "A job with name '" + request.getJobName()
              + "' and group '" + request.getJobGroup() + "' already exists.", e);
    }
  }

  /**
   * Returns all jobs (paginated).
   */
  public Page<ScheduledJob> listJobs(Pageable pageable) {
    return jobRepository.findAll(pageable);
  }

  /**
   * Returns a single job or throws {@link EntityNotFoundException}.
   */
  public ScheduledJob getJob(Long jobId) {
    return jobRepository.findById(jobId)
        .orElseThrow(() -> new EntityNotFoundException("Job not found: " + jobId));
  }

  /**
   * Updates mutable metadata fields of an existing job.
   */
  @Transactional
  public ScheduledJob updateJob(Long jobId, UpdateJobRequest request) {
    ScheduledJob job = getJob(jobId);
    job.setJobName(request.getJobName());
    job.setCronExpression(request.getCronExpression());
    job.setDescription(request.getDescription());
    if (request.getLogRetentionDays() != null) {
      job.setLogRetentionDays(request.getLogRetentionDays());
    }
    job.setNextExecuteTime(calcNextExecuteTime(request.getCronExpression()));
    job.setUpdateTime(LocalDateTime.now());
    return jobRepository.save(job);
  }

  /**
   * Suspends a job so the scheduler skips it.
   */
  @Transactional
  public void pauseJob(Long jobId) {
    ScheduledJob job = getJob(jobId);
    job.setStatus(JobStatus.PAUSED);
    job.setUpdateTime(LocalDateTime.now());
    jobRepository.save(job);
  }

  /**
   * Resumes a suspended job.
   */
  @Transactional
  public void resumeJob(Long jobId) {
    ScheduledJob job = getJob(jobId);
    if (job.getStatus() != JobStatus.PAUSED) {
      throw new IllegalStateException(
          "Job " + jobId + " is not in PAUSED state; current status: " + job.getStatus());
    }
    job.setStatus(JobStatus.READY);
    job.setUpdateTime(LocalDateTime.now());
    jobRepository.save(job);
  }

  /**
   * Schedules a job for immediate execution on the next scan cycle.
   *
   * <p>Only READY or PAUSED jobs may be triggered.  Triggering a RUNNING job
   * would create a duplicate execution; triggering a FAILED job requires a human-intentional resume
   * first.
   */
  @Transactional
  public void triggerJob(Long jobId) {
    ScheduledJob job = getJob(jobId);
    if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.FAILED) {
      throw new IllegalStateException(
          "Job " + jobId + " cannot be triggered in status " + job.getStatus()
              + ". RUNNING jobs must finish first; FAILED jobs must be resumed first.");
    }
    job.setStatus(JobStatus.READY);
    job.setNextExecuteTime(LocalDateTime.now());
    job.setUpdateTime(LocalDateTime.now());
    jobRepository.save(job);
  }

  /**
   * Permanently removes a job and all associated shards and execution logs.
   */
  @Transactional
  public void deleteJob(Long jobId) {
    // Verify existence before cascading deletes.
    getJob(jobId);
    executionLogRepository.deleteByJobId(jobId);
    shardRepository.deleteByJobId(jobId);
    jobRepository.deleteById(jobId);
  }

  /**
   * Returns a paginated execution log for the given job, most recent first.
   */
  public Page<JobExecutionLog> getJobExecutionHistory(Long jobId, Pageable pageable) {
    return executionLogRepository.findByJobIdOrderByStartTimeDesc(jobId, pageable);
  }

  /**
   * Computes summary statistics for a job's recent executions.
   */
  public Map<String, Object> getJobStatistics(Long jobId) {
    // Fetch the 1000 most-recent records, sorted descending so that any cap
    // of the result set always drops the oldest records (not the newest).
    List<JobExecutionLog> logs = executionLogRepository
        .findByJobIdOrderByStartTimeDesc(jobId,
            PageRequest.of(0, 1000, Sort.by("startTime").descending()))
        .getContent();

    long successCount = logs.stream()
        .filter(l -> l.getStatus() == ExecutionStatus.SUCCESS).count();
    long failureCount = logs.stream()
        .filter(l -> l.getStatus() == ExecutionStatus.FAILURE).count();
    double avgExecutionTime = logs.stream()
        .filter(l -> l.getExecutionTime() != null)
        .mapToLong(JobExecutionLog::getExecutionTime)
        .average().orElse(0.0);

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalExecutions", logs.size());
    stats.put("successCount", successCount);
    stats.put("failureCount", failureCount);
    stats.put("successRate", logs.isEmpty() ? 0 : (double) successCount / logs.size() * 100);
    stats.put("avgExecutionTime", avgExecutionTime);
    return stats;
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  /**
   * Calculates the next fire time for a cron expression using Spring's built-in parser. Accepts the
   * standard 6-field format (second minute hour day month weekday).
   *
   * @throws IllegalArgumentException if the expression is invalid
   */
  private LocalDateTime calcNextExecuteTime(String cronExpression) {
    try {
      CronExpression cron = CronExpression.parse(cronExpression);
      LocalDateTime next = cron.next(LocalDateTime.now());
      if (next == null) {
        throw new IllegalArgumentException(
            "Cron expression '" + cronExpression + "' has no future fire times.");
      }
      return next;
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Invalid cron expression '" + cronExpression + "': " + e.getMessage(), e);
    }
  }
}
