package cloud.xcan.sdf.idgen.dao;

import cloud.xcan.sdf.idgen.entity.IdConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * DAO for IdConfig
 *
 * @author liuxiaolong
 */
@Repository
public interface IdConfigRepo extends JpaRepository<IdConfig, String> {

  /**
   * Get {@link IdConfig} by bizKey and tenantId.
   */
  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  IdConfig findByBizKeyAndTenantId(String bizKey, Long tenantId);

  /**
   * Increment Id step by bizKey and tenantId.
   */
  @Modifying
  @Query("update id_config t set t.maxId = t.maxId + ?1 where t.bizKey =?2 and t.tenantId =?3")
  int incrementByBizKeyAndTenantId(Long step, String bizKey, Long tenantId);

  /**
   * Get {@link IdConfig#getMaxId()} by bizKey and tenantId.
   */
  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query("select t.maxId from id_config t where t.bizKey =?1 and t.tenantId =?2")
  long findMaxIdByBizKeyAndTenantId(String bizKey, Long tenantId);

}
