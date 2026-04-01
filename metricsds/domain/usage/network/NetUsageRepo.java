package cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network;

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
public interface NetUsageRepo extends BaseRepository<NetUsage, Long> {

  @Modifying
  @Transactional
  @Override
  @Sharding
  void batchInsert0(Iterable<NetUsage> netUsages);

  @Sharding
  @Query(value = "SELECT DISTINCT device_name FROM node_net_usage WHERE node_id = ?1 AND timestamp > ?2", nativeQuery = true)
  List<String> findDeviceNameByNodeId(Long nodeId,
      /*Reduce the amount of data, otherwise the query will be extremely slow*/
      LocalDateTime fromDate);

  @Deprecated // When the query data range is 1 day, it is still very slow
  @Sharding
  @Query(value = "SELECT DISTINCT device_name FROM node_net_usage WHERE node_id= ?1 AND timestamp >= ?2 AND timestamp <= ?3", nativeQuery = true)
  List<String> findNetDeviceName(Long nodeId, String startTime, String endTime);

  @Sharding
  NetUsage findFirstByNodeIdAndDeviceNameOrderByTimestampDesc(Long nodeId, String diskName);

  @Override
  @Sharding
  Page<NetUsage> findAll(Specification<NetUsage> filters, Pageable pageable);

  @Transactional
  @Modifying
  @Sharding
  @Query(value = "DELETE FROM node_net_usage WHERE timestamp < ?1", nativeQuery = true)
  void deleteBeforeDay(long reservedTime);

}
