package cloud.xcan.angus.job.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Database-backed distributed lock record.
 *
 * <p>Lock acquisition is performed by attempting to insert a new row.
 * The primary-key uniqueness constraint on {@code lock_key} guarantees mutual
 * exclusion: only one node can successfully insert for a given key; concurrent
 * nodes receive a constraint-violation exception and must treat that as "lock
 * already held".
 *
 * <p>Ownership is verified by comparing both {@code owner} (node ID) AND
 * {@code lockValue} (per-acquisition UUID), so lock release cannot be spoofed
 * by knowing only the node ID.
 */
@Entity
@Table(
    name = "distributed_lock",
    indexes = @Index(name = "idx_dl_expire_time", columnList = "expire_time")
)
@Getter
@Setter
public class DistributedLock {

  @Id
  @Column(name = "lock_key", length = 255)
  private String lockKey;

  /** Random UUID generated at lock-acquisition time; used for ownership proof on release. */
  @Column(name = "lock_value", nullable = false, length = 255)
  private String lockValue;

  /** Identifier of the cluster node that holds this lock. */
  @Column(name = "owner", nullable = false, length = 255)
  private String owner;

  @Column(name = "acquire_time", nullable = false)
  private LocalDateTime acquireTime;

  @Column(name = "expire_time", nullable = false)
  private LocalDateTime expireTime;

  @Version
  @Column(name = "version")
  private Long version;
}
