package cloud.xcan.angus.queue.starter.repository;

import cloud.xcan.angus.queue.core.entity.MessageEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  @Modifying
  @Query(value = """
      UPDATE mq_message
      SET status=1,
          lease_until = :leaseUntil,
          lease_owner=:owner,
          updated_at=NOW()
      WHERE id IN (
        SELECT id FROM (
          SELECT id FROM mq_message
          WHERE topic=:topic AND partition_id IN (:partitions)
            AND status=0 AND visible_at <= NOW()
          ORDER BY priority DESC, visible_at ASC, id ASC
          LIMIT :limit
        ) t
      )
      """, nativeQuery = true)
  int leaseBatch(@Param("topic") String topic,
      @Param("partitions") Collection<Integer> partitions,
      @Param("owner") String owner,
      @Param("leaseUntil") Instant leaseUntil,
      @Param("limit") int limit);

  @Modifying
  @Query(value = "UPDATE mq_message SET status=2, updated_at=NOW() WHERE id IN (:ids)", nativeQuery = true)
  int ackBatch(@Param("ids") Collection<Long> ids);

  @Modifying
  @Query(value = """
      UPDATE mq_message
      SET status=0,
          attempts=attempts+1,
          visible_at = :newVisibleAt,
          lease_owner=NULL,
          lease_until=NULL,
          updated_at=NOW()
      WHERE id IN (:ids)
      """, nativeQuery = true)
  int nackBatch(@Param("ids") Collection<Long> ids, @Param("newVisibleAt") Instant newVisibleAt);

  @Modifying
  @Query(value = """
      UPDATE mq_message
      SET status=0,
          lease_owner=NULL,
          lease_until=NULL,
          updated_at=NOW()
      WHERE id IN (
        SELECT id FROM (
          SELECT id FROM mq_message
          WHERE status=1 AND lease_until < NOW()
          ORDER BY lease_until ASC, id ASC
          LIMIT :limit
        ) t
      )
      """, nativeQuery = true)
  int reclaimExpiredLeases(@Param("limit") int limit);

  @Query(value = """
      SELECT * FROM mq_message
      WHERE lease_owner=:owner AND lease_until >= :now AND status=1
      ORDER BY priority DESC, visible_at ASC, id ASC
      LIMIT :limit
      """, nativeQuery = true)
  List<MessageEntity> findLeasedByOwner(@Param("owner") String owner,
      @Param("now") Instant now,
      @Param("limit") int limit);

  @Query(value = "SELECT status, COUNT(*) cnt FROM mq_message WHERE topic=:topic GROUP BY status", nativeQuery = true)
  List<Object[]> countByStatus(@Param("topic") String topic);

  @Query(value = """
      SELECT partition_id, COUNT(*) cnt
      FROM mq_message
      WHERE topic=:topic AND status=0 AND visible_at <= NOW()
      GROUP BY partition_id
      ORDER BY partition_id
      """, nativeQuery = true)
  List<Object[]> readyCountPerPartition(@Param("topic") String topic);

  @Modifying
  @Query(value = "DELETE FROM mq_message WHERE topic=:topic AND status=2 AND updated_at < :before", nativeQuery = true)
  int purgeDoneBefore(@Param("topic") String topic, @Param("before") Instant before);
}
