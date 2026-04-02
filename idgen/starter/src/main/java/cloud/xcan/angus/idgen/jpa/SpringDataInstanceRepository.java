package cloud.xcan.angus.idgen.jpa;

import cloud.xcan.angus.idgen.entity.Instance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataInstanceRepository extends JpaRepository<Instance, String> {

  @Lock(value = LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from angus_instance t where t.host = ?1 and t.port = ?2")
  Instance findByHostAndPort(String host, String port);

  @Modifying
  @Query("update angus_instance t set t.id = t.id + 1 where t.pk = ?1 and t.id = ?2")
  int incrementId(String pk, Long id);
}
