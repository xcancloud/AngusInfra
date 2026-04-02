package cloud.xcan.angus.sharding.autoconfigure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * JPA entity that tracks physically-created shard tables in the <em>primary</em> datasource.
 *
 * <p>To enable JPA-backed shard table persistence, include this entity in your primary
 * entity manager scan (or ensure its package is covered by your {@code @EnableJpaRepositories}
 * configuration) and register a bean of type {@link ShardTableJpaRepository}.  The framework will
 * then automatically use {@link JpaShardTableRegistry} instead of the in-memory default.
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * @SpringBootApplication
 * @EntityScan(basePackageClasses = {ShardTableEntity.class, ...})
 * @EnableJpaRepositories(basePackageClasses = {ShardTableJpaRepository.class, ...})
 * public class MyApplication { ... }
 * }</pre>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "tableName")
@Accessors(chain = true)
@Entity
@Table(name = "angus_shard_table")
public class ShardTableEntity {

  /**
   * Fully-qualified sharded table name (primary key), e.g. {@code exec_sample-100-3}.
   */
  @Id
  @Column(name = "table_name", nullable = false, length = 255)
  private String tableName;

  /**
   * The shard key used to determine the target DB shard.
   */
  @Column(name = "shard_key", nullable = false)
  private long shardKey;

  /**
   * Zero-based DB shard index.
   */
  @Column(name = "db_index", nullable = false)
  private int dbIndex;

  /**
   * Secondary table index; {@code -1} indicates no secondary index.
   */
  @Column(name = "table_index", nullable = false)
  private long tableIndex = -1;
}
