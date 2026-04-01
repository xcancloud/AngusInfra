package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MtItEmployeeRepository extends JpaRepository<MtItEmployee, Long>,
    JpaSpecificationExecutor<MtItEmployee> {

  /**
   * 派生查询：穿透关联对象属性
   */
  List<MtItEmployee> findByDepartment_Name(String departmentName);

  long countByName(String name);

  /**
   * JPQL：join fetch 部门
   */
  @Query("select e from MtItEmployee e join fetch e.department d where d.name = :deptName")
  List<MtItEmployee> findAllByDepartmentNameJoinFetch(@Param("deptName") String deptName);

  /**
   * JPQL：聚合 count + join
   */
  @Query("select count(e) from MtItEmployee e join e.department d where d.name = :deptName")
  long countByDepartmentName(@Param("deptName") String deptName);

  /**
   * JPQL 批量更新。Hibernate {@code @Filter} 不会作用于 bulk update/delete，须在 WHERE 中显式限定租户。
   */
  @Modifying(clearAutomatically = true)
  @Query("update MtItEmployee e set e.name = :newName where e.name = :oldName and e.tenantId = :tenantId")
  int updateNameJpql(@Param("oldName") String oldName, @Param("newName") String newName,
      @Param("tenantId") Long tenantId);

  /**
   * 原生：按租户显式过滤
   */
  @Query(value = "select e.name from mt_it_employee e where e.tenant_id = :tid order by e.name",
      nativeQuery = true)
  List<String> listNamesNativeForTenant(@Param("tid") Long tid);
}
