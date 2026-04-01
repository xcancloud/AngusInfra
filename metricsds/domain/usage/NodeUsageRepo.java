package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface NodeUsageRepo extends BaseRepository<NodeUsage, Long> {

  @Transactional
  @Override
  @Sharding
  void batchInsert0(Iterable<NodeUsage> nodeUsages);

  @Transactional
  @Override
  @Modifying
  @Sharding
  NodeUsage save(NodeUsage nodeUsage);

  @Override
  @Sharding
  Page<NodeUsage> findAll(Specification<NodeUsage> filters, Pageable pageable);

  @Sharding
  NodeUsage findFirstByNodeIdOrderByTimestampDesc(Long id);

  @Sharding
  @Query(value = "SELECT node_id FROM node_usage WHERE timestamp >= ?1 AND node_id IN ?2", nativeQuery = true)
  Set<Long> findLatestIdByTimestampBeforeAndNodeIdIn(Long before, Collection<Long> ids);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM node_usage WHERE timestamp < ?1", nativeQuery = true)
  void deleteBeforeDay(long reservedTime);

}
