package cloud.xcan.angus.job.jpa;

import cloud.xcan.angus.job.entity.JobShard;
import cloud.xcan.angus.job.enums.ShardStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA jpa for {@link JobShard}.
 */
@Repository
public interface JobShardRepository extends JpaRepository<JobShard, Long> {

  List<JobShard> findByJobIdAndStatus(Long jobId, ShardStatus status);

  List<JobShard> findByJobId(Long jobId);

  /**
   * Bulk-deletes all shards for the given job. Used before creating a fresh set of shards for a new
   * execution run.
   */
  @Modifying
  @Query("DELETE FROM JobShard s WHERE s.jobId = :jobId")
  void deleteByJobId(@Param("jobId") Long jobId);
}
