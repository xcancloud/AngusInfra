package cloud.xcan.angus.idgen.jpa;

import cloud.xcan.angus.idgen.entity.IdConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIdConfigRepository extends JpaRepository<IdConfig, String> {

  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  IdConfig findByBizKeyAndTenantId(String bizKey, Long tenantId);

  @Modifying
  @Query("update id_config t set t.maxId = t.maxId + ?1 where t.bizKey =?2 and t.tenantId =?3")
  int incrementByBizKeyAndTenantId(Long step, String bizKey, Long tenantId);

  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query("select t.maxId from id_config t where t.bizKey =?1 and t.tenantId =?2")
  long findMaxIdByBizKeyAndTenantId(String bizKey, Long tenantId);
}
