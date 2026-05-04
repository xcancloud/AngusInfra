package cloud.xcan.angus.job.monitor;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import cloud.xcan.angus.job.jpa.DistributedLockRepository;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Background monitor for self-healing and observability.
 *
 * <h3>P1 fixes applied</h3>
 * <ul>
 *   <li>{@link #cleanExpiredLocks()} uses a bulk-delete JPQL query instead of
 *       {@code findAll()} followed by in-memory filter — avoids OOM on large tables.</li>
 *   <li>{@link #monitorTimeoutJobs()} queries jobs whose {@code startTime} is
 *       <em>before</em> the timeout threshold (was erroneously querying <em>after</em>),
 *       so timeout detection now actually fires.</li>
 *   <li>{@link #generateHealthReport()} uses a time-range query rather than a full
 *       table scan; the SLF4J format string no longer uses Python-style {@code {:.2f}}
 *       formatting which SLF4J does not multitenancy.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobHealthMonitor {

  private final JobExecutionLogRepository executionLogRepository;
  private final DistributedLockRepository lockRepository;
  private final JobProperties properties;

  /**
   * Purges all expired distributed-lock rows in one batch DELETE. Originally used {@code findAll()}
   * which could OOM on large datasets.
   */
  @Scheduled(fixedDelay = 60_000)
  @Transactional
  public void cleanExpiredLocks() {
    try {
      int deleted = lockRepository.deleteExpiredLocks(LocalDateTime.now());
      if (deleted > 0) {
        log.info("Cleaned {} expired distributed lock(s)", deleted);
      }
    } catch (Exception e) {
      log.error("Failed to clean expired locks", e);
    }
  }

  /**
   * Detects executions that have been in RUNNING state longer than the configured threshold.
   *
   * <p>The query looks for {@code startTime BEFORE (now - threshold)} — jobs that
   * started before the threshold are the ones potentially timed out.  The original implementation
   * queried {@code startTime AFTER threshold}, which would never match long-running jobs.
   */
  @Scheduled(fixedDelay = 30_000)
  public void monitorTimeoutJobs() {
    try {
      LocalDateTime timeoutThreshold = LocalDateTime.now()
          .minusMinutes(properties.getTimeoutThresholdMinutes());

      // Fix: use findByStatusAndStartTimeBefore (not After) to find stale RUNNING records.
      List<JobExecutionLog> timedOutJobs = executionLogRepository
          .findByStatusAndStartTimeBefore(ExecutionStatus.RUNNING, timeoutThreshold);

      if (!timedOutJobs.isEmpty()) {
        log.warn("Found {} job execution(s) potentially timed out (running > {} minutes)",
            timedOutJobs.size(), properties.getTimeoutThresholdMinutes());
        for (JobExecutionLog entry : timedOutJobs) {
          log.warn("Timed-out execution: job={} shardingItem={} startedAt={}",
              entry.getJobName(), entry.getShardingItem(), entry.getStartTime());
        }
      }
    } catch (Exception e) {
      log.error("Failed to monitor timeout jobs", e);
    }
  }

  /**
   * Logs an hourly health summary.
   *
   * <p>Queries <em>all</em> statuses in the last-hour window via
   * {@code findByStartTimeBetween} — the previous implementation incorrectly queried only
   * {@code RUNNING} logs and then counted {@code SUCCESS}/ {@code FAILURE} within that set, which
   * always returned zero.
   */
  @Scheduled(cron = "0 0 * * * *")
  public void generateHealthReport() {
    try {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime oneHourAgo = now.minusHours(1);

      // Query ALL statuses (SUCCESS, FAILURE, RUNNING) within the last hour.
      List<JobExecutionLog> recentExecutions = executionLogRepository
          .findByStartTimeBetween(oneHourAgo, now);

      long totalExecutions = recentExecutions.size();
      long successCount = recentExecutions.stream()
          .filter(l -> l.getStatus() == ExecutionStatus.SUCCESS).count();
      long failureCount = recentExecutions.stream()
          .filter(l -> l.getStatus() == ExecutionStatus.FAILURE).count();

      // Fix: use String.format for floating-point formatting; SLF4J placeholders do not
      // multitenancy printf-style specifiers like {:.2f}.
      double successRate = totalExecutions > 0
          ? (double) successCount / totalExecutions * 100
          : 0.0;

      log.info("=== Job Scheduler Health Report (last hour) ===");
      log.info("Total Executions : {}", totalExecutions);
      log.info("  Success        : {}", successCount);
      log.info("  Failure        : {}", failureCount);
      log.info("  Success Rate   : {}", String.format("%.2f%%", successRate));
      log.info("===============================================");

    } catch (Exception e) {
      log.error("Failed to generate health report", e);
    }
  }
}
