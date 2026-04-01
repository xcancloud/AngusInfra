package cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice;

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
public interface JvmServiceUsageRepo extends BaseRepository<JvmServiceUsage, Long> {

  @Override
  @Transactional
  @Modifying
  @Sharding
  void batchInsert0(Iterable<JvmServiceUsage> usages);

  @Override
  @Transactional
  @Modifying
  @Sharding
  JvmServiceUsage save(JvmServiceUsage usage);

  @Override
  @Sharding
  Page<JvmServiceUsage> findAll(Specification<JvmServiceUsage> var1, Pageable pageable);

  @Sharding
  @Query(value = "SELECT service_id FROM jvm_service_usage WHERE timestamp >= ?1 AND service_id IN ?2", nativeQuery = true)
  Set<Long> findLatestIdByTimestampBeforeAndServiceIdIn(Long before, Collection<Long> ids);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM jvm_service_usage WHERE timestamp < ?1", nativeQuery = true)
  void deleteBeforeDay(long reservedTime);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM jvm_service_usage WHERE service_id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> serviceIds);

}
