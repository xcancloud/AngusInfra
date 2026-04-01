package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecSampleErrorCauseRepo extends BaseRepository<ExecSampleErrorCause, Long> {

  @Override
  @Transactional
  @Modifying
  @Sharding
  void batchInsert0(Iterable<ExecSampleErrorCause> usages);

  @Override
  @Sharding
  Page<ExecSampleErrorCause> findAll(Specification spec, Pageable pageable);

  @Sharding
  List<ExecSampleErrorCause> findByExecId(Long id);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample_error_cause WHERE timestamp < DATE_SUB(CURDATE(), INTERVAL ?1 DAY)", nativeQuery = true)
  void deleteBeforeDay(long reservedDay);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample_error_cause WHERE exec_id = ?1", nativeQuery = true)
  void deleteByExecId(Long execId);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample_error_cause WHERE exec_id IN ?1", nativeQuery = true)
  void deleteByExecIdIn(Collection<Long> execIds);

}
