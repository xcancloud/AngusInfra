package cloud.xcan.angus.job.jpa;

import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ScheduledJob}.
 */
@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

  /**
   * Finds all jobs due for execution: status is READY and scheduled time is in the past.
   *
   * @param status READY
   * @param time   current time
   * @return list of overdue jobs
   */
  List<ScheduledJob> findByStatusAndNextExecuteTimeBefore(JobStatus status, LocalDateTime time);

  /**
   * Bounded variant — returns at most {@code pageable.getPageSize()} due jobs. Used by the
   * scheduler to prevent loading an unbounded result set when many jobs become due simultaneously.
   */
  List<ScheduledJob> findByStatusAndNextExecuteTimeBefore(JobStatus status, LocalDateTime time,
      Pageable pageable);

  Optional<ScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);
}
