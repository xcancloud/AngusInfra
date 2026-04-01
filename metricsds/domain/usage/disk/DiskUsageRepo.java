package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface DiskUsageRepo extends BaseRepository<DiskUsage, Long> {

  @Transactional
  @Override
  @Sharding
  void batchInsert0(Iterable<DiskUsage> diskUsages);

  @Sharding
  @Query(value = "SELECT DISTINCT device_name FROM node_disk_usage WHERE node_id = ?1 AND timestamp > ?2", nativeQuery = true)
  List<String> findDeviceNameByNodeId(Long nodeId,
      LocalDateTime fromDate /*Reduce the amount of data, otherwise the query will be extremely slow*/);

  @Deprecated // When the query data range is 1 day, it is still very slow
  @Sharding
  @Query(value =
      "SELECT DISTINCT device_name FROM node_disk_usage WHERE node_id= ?1 "
          + "AND CASE WHEN ?2 IS NOT NULL THEN timestamp >= ?2 ELSE 1=1 END AND CASE WHEN ?3 IS NOT NULL THEN timestamp <= ?3 ELSE 1=1 END "
          + "AND CASE WHEN ?4 IS NOT NULL THEN device_name = ?4 ELSE 1=1 END ", nativeQuery = true)
  List<String> findDiskDeviceNames(Long nodeId, String startTime, String endTime,
      String deviceName);

  @Sharding
  DiskUsage findFirstByNodeIdAndDeviceNameOrderByTimestampDesc(Long id, String deviceName);

  @Override
  @Sharding
  Page<DiskUsage> findAll(Specification<DiskUsage> filters, Pageable pageable);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM node_disk_usage WHERE timestamp < ?1", nativeQuery = true)
  void deleteBeforeDay(long reservedTime);

}
