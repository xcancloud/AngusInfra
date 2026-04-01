package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecSampleRepo extends BaseRepository<ExecSample, Long> {

  @Override
  @Transactional
  @Modifying
  @Sharding
  void batchInsert0(Iterable<ExecSample> usages);

  @Sharding
  @Query(value =
      "SELECT * FROM exec_sample WHERE exec_id = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  ExecSample findLatestByExecId(Long execId);

  @Sharding
  @Query(value = "SELECT node_id FROM exec_sample WHERE exec_id = ?1 ORDER BY timestamp ASC LIMIT 1", nativeQuery = true)
  Long findFirstNodeByExecId(Long execId);

  @Sharding
  @Query(value =
      "SELECT * FROM exec_sample WHERE exec_id = ?1 AND name = ?2 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  ExecSample findLatestByExecIdAndName(Long execId, String name);

  @Sharding
  @Query(value =
      "SELECT * FROM exec_sample WHERE exec_id = ?1 AND name = ?2 AND ramp_num = ?3 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  ExecSample findLatestByExecIdAndNameAndRampNum(Long execId, String name, int rampNum);

  @Sharding
  @Query(value =
      "SELECT timestamp FROM exec_sample WHERE exec_id = ?1 AND name = ?2 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  Long findLatestTimestampByExecIdAndName(Long execId, String name);

  @Sharding
  @Query(value =
      "SELECT timestamp FROM exec_sample WHERE exec_id = ?1 AND name = ?2 AND ramp_num = ?3 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  Long findLatestTimestampByExecIdAndNameAndRampNum(Long execId, String name, int rampNum);

  @Sharding
  @Query(value = "SELECT * FROM exec_sample WHERE exec_id = ?1 AND name = ?2 AND timestamp >= ?3 ORDER BY timestamp DESC", nativeQuery = true)
  List<ExecSample> findLatestByExecIdAndNameAndLastSampleDateAndReportInterval(
      Long execId, String name, Long lastSampleDate);

  @Sharding
  @Query(value = "SELECT * FROM exec_sample WHERE exec_id = ?1 AND name = ?2 AND timestamp >= ?3 AND ramp_num = ?4 ORDER BY timestamp DESC", nativeQuery = true)
  List<ExecSample> findLatestByExecIdAndNameAndLastSampleDateAndReportIntervalAndRampNum(
      Long execId, String name, Long lastSampleDate, int rampNum);

  @Sharding
  @Query(value = "SELECT distinct ramp_num FROM exec_sample WHERE exec_id = ?1", nativeQuery = true)
  List<Integer> findRampNum(Long execId);

  @Sharding
  @Query(value =
      "SELECT finish, node_id nodeId, upload_result_bytes uploadResultBytes, upload_result_total_bytes uploadResultTotalBytes "
          + "FROM exec_sample WHERE exec_id = ?1 AND name = 'Total' "
          + "ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
  ExecSampleUploadResultProgress findLastSampleUploadResultProgress(Long execId);

  @Sharding
  @Query(value =
      "SELECT t.name, t.node_id nodeId, t.timestamp, t.error_cause_counter counter "
          + " FROM exec_sample t INNER JOIN ("
          + "    SELECT name, MAX(timestamp) AS latest_time"
          + "      FROM exec_sample "
          + "         WHERE exec_id = ?1 "
          // + "           AND CASE WHEN ?3 IS NOT NULL THEN node_id = ?3 ELSE 1=1 END " // Performance issue, not actually used
          // + "           AND CASE WHEN ?4 IS NOT NULL THEN name = ?4 ELSE 1=1 END "
          + "       GROUP BY name, node_id"
          + ") sub ON t.name = sub.name AND t.timestamp = sub.latest_time", nativeQuery = true)
  List<ExecSampleCounter> findLatestErrorCauseCounter(Long execId, Long nodeId, String name);

  @Sharding
  @Query(value =
      "SELECT t.name, t.node_id nodeId, t.timestamp, t.ext_counter_map1 counter "
          + " FROM exec_sample t INNER JOIN ("
          + "    SELECT name, MAX(timestamp) AS latest_time"
          + "      FROM exec_sample "
          + "         WHERE exec_id = ?1 "
          //+ "           AND CASE WHEN ?3 IS NOT NULL THEN node_id = ?3 ELSE 1=1 END " // Performance issue, not actually used
          //+ "           AND CASE WHEN ?4 IS NOT NULL THEN name = ?4 ELSE 1=1 END "
          + "       GROUP BY name, node_id"
          + ") sub ON t.name = sub.name AND t.timestamp = sub.latest_time", nativeQuery = true)
  List<ExecSampleCounter> findLatestExtCounterMap1(Long execId, Long nodeId, String name);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample WHERE timestamp < DATE_SUB(CURDATE(), INTERVAL ?1 DAY)", nativeQuery = true)
  void deleteBeforeDay(long reservedDay);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample WHERE exec_id = ?1", nativeQuery = true)
  void deleteByExecId(Long execId);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM exec_sample WHERE exec_id IN ?1", nativeQuery = true)
  void deleteByExecIdIn(Collection<Long> execIds);
}
