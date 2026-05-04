package cloud.xcan.angus.sharding.jpa;

import cloud.xcan.angus.sharding.JpaShardTableRegistry;
import cloud.xcan.angus.sharding.entity.ShardTableEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA jpa for {@link ShardTableEntity}.
 *
 * <p>When this interface is registered as a Spring bean (either directly via
 * {@code @EnableJpaRepositories} or through component scanning), the framework automatically
 * activates {@link JpaShardTableRegistry} as the
 * {@link cloud.xcan.angus.sharding.table.ShardTableRegistry} implementation.
 *
 * <p>This jpa operates against the <em>primary</em> datasource / entity manager factory
 * – it must not reference the sharding entity manager factory.
 */
@Repository
public interface ShardTableJpaRepository extends JpaRepository<ShardTableEntity, String> {

  /**
   * Returns all shard table entities matching the given shard key.
   */
  List<ShardTableEntity> findByShardKey(long shardKey);
}
