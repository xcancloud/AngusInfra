package cloud.xcan.angus.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobStatus;
import cloud.xcan.angus.job.enums.JobType;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.model.UpdateJobRequest;
import cloud.xcan.angus.job.repository.JobExecutionLogRepository;
import cloud.xcan.angus.job.repository.JobShardRepository;
import cloud.xcan.angus.job.repository.ScheduledJobRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobManagementServiceTest {

  @Mock
  private ScheduledJobRepository jobRepository;
  @Mock
  private JobExecutionLogRepository executionLogRepository;
  @Mock
  private JobShardRepository shardRepository;

  @InjectMocks
  private JobManagementService service;

  // ---------------------------------------------------------------------------
  // createJob
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("createJob persists a well-formed request")
  void createJob_success() {
    CreateJobRequest req = new CreateJobRequest();
    req.setJobName("TestJob");
    req.setJobGroup("G1");
    req.setCronExpression("0 * * * * *"); // every minute
    req.setBeanName("myExecutor");
    req.setJobType(JobType.SIMPLE);

    when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ScheduledJob created = service.createJob(req);

    assertThat(created.getJobName()).isEqualTo("TestJob");
    assertThat(created.getStatus()).isEqualTo(JobStatus.READY);
    assertThat(created.getRetryCount()).isEqualTo(0);
    assertThat(created.getNextExecuteTime()).isNotNull()
        .isAfter(LocalDateTime.now().minusSeconds(1));
  }

  @Test
  @DisplayName("createJob throws IllegalArgumentException on invalid cron")
  void createJob_invalidCron() {
    CreateJobRequest req = new CreateJobRequest();
    req.setJobName("Bad");
    req.setJobGroup("G1");
    req.setCronExpression("NOT_A_CRON");
    req.setBeanName("myExecutor");
    req.setJobType(JobType.SIMPLE);

    assertThatThrownBy(() -> service.createJob(req))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ---------------------------------------------------------------------------
  // getJob
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("getJob throws EntityNotFoundException for unknown id")
  void getJob_notFound() {
    when(jobRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getJob(99L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("99");
  }

  // ---------------------------------------------------------------------------
  // pauseJob / resumeJob
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("pauseJob changes status to PAUSED")
  void pauseJob() {
    ScheduledJob job = readyJob(1L);
    when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.pauseJob(1L);

    assertThat(job.getStatus()).isEqualTo(JobStatus.PAUSED);
  }

  @Test
  @DisplayName("resumeJob changes status from PAUSED to READY")
  void resumeJob_success() {
    ScheduledJob job = readyJob(1L);
    job.setStatus(JobStatus.PAUSED);
    when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.resumeJob(1L);

    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
  }

  @Test
  @DisplayName("resumeJob throws when job is not PAUSED")
  void resumeJob_notPaused() {
    ScheduledJob job = readyJob(1L);
    job.setStatus(JobStatus.READY);
    when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> service.resumeJob(1L))
        .isInstanceOf(IllegalStateException.class);
  }

  // ---------------------------------------------------------------------------
  // triggerJob
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("triggerJob rejects RUNNING jobs")
  void triggerJob_rejectsRunning() {
    ScheduledJob job = readyJob(2L);
    job.setStatus(JobStatus.RUNNING);
    when(jobRepository.findById(2L)).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> service.triggerJob(2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RUNNING");
  }

  @Test
  @DisplayName("triggerJob rejects FAILED jobs")
  void triggerJob_rejectsFailed() {
    ScheduledJob job = readyJob(3L);
    job.setStatus(JobStatus.FAILED);
    when(jobRepository.findById(3L)).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> service.triggerJob(3L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FAILED");
  }

  @Test
  @DisplayName("triggerJob sets nextExecuteTime to now for a READY job")
  void triggerJob_success() {
    ScheduledJob job = readyJob(4L);
    when(jobRepository.findById(4L)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    LocalDateTime before = LocalDateTime.now();
    service.triggerJob(4L);

    assertThat(job.getNextExecuteTime()).isAfterOrEqualTo(before);
    assertThat(job.getStatus()).isEqualTo(JobStatus.READY);
  }

  // ---------------------------------------------------------------------------
  // deleteJob — cascade
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("deleteJob cascades to execution logs and shards")
  void deleteJob_cascade() {
    ScheduledJob job = readyJob(5L);
    when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

    service.deleteJob(5L);

    verify(executionLogRepository).deleteByJobId(5L);
    verify(shardRepository).deleteByJobId(5L);
    verify(jobRepository).deleteById(5L);
  }

  @Test
  @DisplayName("deleteJob does not attempt cascade for non-existent job")
  void deleteJob_notFound() {
    when(jobRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteJob(99L))
        .isInstanceOf(EntityNotFoundException.class);

    verify(executionLogRepository, never()).deleteByJobId(any());
  }

  // ---------------------------------------------------------------------------
  // updateJob
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("updateJob recalculates nextExecuteTime on cron change")
  void updateJob_recalcNextExecuteTime() {
    ScheduledJob job = readyJob(6L);
    when(jobRepository.findById(6L)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UpdateJobRequest req = new UpdateJobRequest();
    req.setJobName("UpdatedName");
    req.setCronExpression("0 0 * * * *"); // every hour

    ScheduledJob updated = service.updateJob(6L, req);

    assertThat(updated.getJobName()).isEqualTo("UpdatedName");
    assertThat(updated.getNextExecuteTime()).isNotNull();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static ScheduledJob readyJob(Long id) {
    ScheduledJob job = new ScheduledJob();
    job.setId(id);
    job.setJobName("job-" + id);
    job.setJobGroup("grp");
    job.setStatus(JobStatus.READY);
    job.setCronExpression("0 * * * * *");
    job.setBeanName("myExecutor");
    job.setJobType(JobType.SIMPLE);
    job.setRetryCount(0);
    job.setMaxRetryCount(3);
    return job;
  }
}
