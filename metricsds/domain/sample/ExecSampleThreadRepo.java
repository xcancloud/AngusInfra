package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecSampleThreadRepo extends BaseRepository<ExecSampleThread, Long> {

  @Sharding
  @Override
  Page<ExecSampleThread> findAll(Specification<ExecSampleThread> spec, Pageable pageable);

  @Deprecated // Million data volume search data performance for more than 5 seconds
  @Sharding
  @Query(value =
      "SELECT ep.* FROM exec_sample ep WHERE ep.tenant_id = ?1 AND ep.exec_id = ?2 "
          + "AND CASE WHEN ?3 IS NOT NULL THEN ep.timestamp >= ?3 ELSE 1=1 END "
          + "AND CASE WHEN ?4 IS NOT NULL THEN ep.timestamp <= ?4 ELSE 1=1 END "
          + "AND CASE WHEN ?5 IS NOT NULL THEN ep.name IN ?5 ELSE 1=1 END "
          + "AND CASE WHEN ?6 IS NOT NULL THEN ep.node_id IN ?6 ELSE 1=1 END "
          + "ORDER BY ep.timestamp ASC",
      nativeQuery = true)
  List<ExecSampleThread> findAllByTenantIdAndExecIdAndTimestampAndNameInAndNodeIdIn(long tenantId,
      long execId, String startTime, String endTime, Collection<String> names,
      Collection<Long> nodeIds);

  @Deprecated // Million data volume search data performance for more than 5 seconds
  @Sharding
  @Query(value =
      "SELECT ep.* FROM exec_sample ep WHERE ep.tenant_id = ?1 AND ep.exec_id = ?2 "
          + "AND CASE WHEN ?3 IS NOT NULL THEN ep.timestamp >= ?3 ELSE 1=1 END "
          + "AND CASE WHEN ?4 IS NOT NULL THEN ep.timestamp <= ?4 ELSE 1=1 END "
          + "AND CASE WHEN ?5 IS NOT NULL THEN ep.name NOT IN ?5 ELSE 1=1 END "
          + "AND CASE WHEN ?6 IS NOT NULL THEN ep.node_id IN ?6 ELSE 1=1 END "
          + "ORDER BY ep.timestamp ASC",
      nativeQuery = true)
  List<ExecSampleThread> findAllByTenantIdAndExecIdAndTimestampAndNameNotAndNodeIdIn(long tenantId,
      long execId, String startTime, String endTime, String name, Collection<Long> nodeIds);

  @Override
  @Sharding
  List<ExecSampleThread> findAllByFilters(Set<SearchCriteria> filters);

}
