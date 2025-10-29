package cloud.xcan.angus.queue.starter.repository;

import cloud.xcan.angus.queue.core.entity.DeadLetterEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeadLetterRepository extends JpaRepository<DeadLetterEntity, Long> {

  @Query(value = "SELECT COUNT(*) FROM mq_dead_letter WHERE topic=:topic AND deleted_at IS NULL", nativeQuery = true)
  long countByTopic(@Param("topic") String topic);

  @Query(value = "SELECT * FROM mq_dead_letter WHERE topic=:topic AND deleted_at IS NULL LIMIT CAST(:limit AS INTEGER)", nativeQuery = true)
  java.util.List<DeadLetterEntity> findByTopicLimit(@Param("topic") String topic,
      @Param("limit") int limit);

  @Modifying
  @Query(value = "DELETE FROM mq_dead_letter WHERE topic=:topic", nativeQuery = true)
  int hardDeleteByTopic(@Param("topic") String topic);

  @Modifying
  @Query(value = "UPDATE mq_dead_letter SET deleted_at = NOW() WHERE topic=:topic AND deleted_at IS NULL", nativeQuery = true)
  int softDeleteByTopic(@Param("topic") String topic);

  @Modifying
  @Query(value = "DELETE FROM mq_dead_letter WHERE deleted_at IS NOT NULL AND deleted_at < :before", nativeQuery = true)
  int purgeSoftDeletedBefore(@Param("before") Instant before);
}
