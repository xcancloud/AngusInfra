package cloud.xcan.angus.idgen.dao;

import cloud.xcan.angus.idgen.entity.Instance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * DAO for InstanceInfo
 *
 * @author liuxiaolong
 */
@Repository
public interface InstanceRepo extends JpaRepository<Instance, String> {

  /**
   * Get {@link Instance} by host and port
   */
  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query(value = "select t from instance t where t.host =?1 and t.port =?2 ")
  Instance findByHostAndPort(String host, String port);

  /**
   * Increment Id
   */
  @Modifying
  @Query("update instance t set t.id = t.id + 1 where t.pk=?1 and t.id =?2")
  int incrementId(String pk, Long id);

}
