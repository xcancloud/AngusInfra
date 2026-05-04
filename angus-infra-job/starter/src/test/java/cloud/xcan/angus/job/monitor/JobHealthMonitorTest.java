package cloud.xcan.angus.job.monitor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.job.entity.JobExecutionLog;
import cloud.xcan.angus.job.enums.ExecutionStatus;
import cloud.xcan.angus.job.jpa.DistributedLockRepository;
import cloud.xcan.angus.job.jpa.JobExecutionLogRepository;
import cloud.xcan.angus.job.properties.JobProperties;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobHealthMonitorTest {

  @Mock
  private JobExecutionLogRepository executionLogRepository;
  @Mock
  private DistributedLockRepository lockRepository;
  @Mock
  private JobProperties properties;

  private JobHealthMonitor monitor;

  @BeforeEach
  void setUp() {
    monitor = new JobHealthMonitor(executionLogRepository, lockRepository, properties);
  }

  @Test
  void cleanExpiredLocks_deletesAndLogs() {
    when(lockRepository.deleteExpiredLocks(any(LocalDateTime.class))).thenReturn(2);
    monitor.cleanExpiredLocks();
    verify(lockRepository).deleteExpiredLocks(any(LocalDateTime.class));
  }

  @Test
  void cleanExpiredLocks_swallowsErrors() {
    when(lockRepository.deleteExpiredLocks(any(LocalDateTime.class)))
        .thenThrow(new RuntimeException("db"));
    monitor.cleanExpiredLocks();
  }

  @Test
  void monitorTimeoutJobs_queriesRunningBeforeThreshold() {
    when(properties.getTimeoutThresholdMinutes()).thenReturn(30);
    JobExecutionLog log = new JobExecutionLog();
    log.setJobName("j");
    log.setShardingItem(0);
    log.setStartTime(LocalDateTime.now().minusHours(1));
    when(executionLogRepository.findByStatusAndStartTimeBefore(
        eq(ExecutionStatus.RUNNING), any(LocalDateTime.class)))
        .thenReturn(List.of(log));

    monitor.monitorTimeoutJobs();

    verify(executionLogRepository).findByStatusAndStartTimeBefore(
        eq(ExecutionStatus.RUNNING), any(LocalDateTime.class));
  }

  @Test
  void monitorTimeoutJobs_swallowsErrors() {
    when(properties.getTimeoutThresholdMinutes()).thenReturn(1);
    when(executionLogRepository.findByStatusAndStartTimeBefore(any(), any()))
        .thenThrow(new RuntimeException("x"));
    monitor.monitorTimeoutJobs();
  }

  @Test
  void generateHealthReport_withExecutions() {
    JobExecutionLog ok = new JobExecutionLog();
    ok.setStatus(ExecutionStatus.SUCCESS);
    JobExecutionLog bad = new JobExecutionLog();
    bad.setStatus(ExecutionStatus.FAILURE);
    when(executionLogRepository.findByStartTimeBetween(any(), any()))
        .thenReturn(List.of(ok, bad));

    monitor.generateHealthReport();

    verify(executionLogRepository).findByStartTimeBetween(any(), any());
  }

  @Test
  void generateHealthReport_swallowsErrors() {
    when(executionLogRepository.findByStartTimeBetween(any(), any()))
        .thenThrow(new RuntimeException("x"));
    monitor.generateHealthReport();
  }
}
