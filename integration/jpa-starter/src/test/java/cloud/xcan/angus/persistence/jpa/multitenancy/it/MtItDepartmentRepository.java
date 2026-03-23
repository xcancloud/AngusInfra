package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MtItDepartmentRepository extends JpaRepository<MtItDepartment, Long> {

  Optional<MtItDepartment> findByName(String name);

  /**
   * JPQL：部门 ↔ 员工集合 join，按员工名模糊查部门
   */
  @Query("select distinct d from MtItDepartment d join d.employees e where e.name like concat('%', :kw, '%')")
  List<MtItDepartment> findDepartmentsByEmployeeNameContaining(@Param("kw") String kw);

  /**
   * JPQL fetch join；WHERE 须显式 {@code tenantId}，避免跨租户同名部门多条命中。
   */
  @Query("select distinct d from MtItDepartment d join fetch d.employees e "
      + "where d.name = :name and d.tenantId = :tenantId")
  Optional<MtItDepartment> findWithEmployeesByName(@Param("name") String name,
      @Param("tenantId") Long tenantId);

  /**
   * 原生 SQL：须显式带 tenant_id；Hibernate {@code @Filter} 不作用于 native query。
   */
  @Query(value = "select d.name from mt_it_department d where d.tenant_id = :tid and d.name = :n",
      nativeQuery = true)
  Optional<String> findNameNativeForTenant(@Param("tid") long tid, @Param("n") String name);
}
