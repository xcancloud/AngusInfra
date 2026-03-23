package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.persistence.jpa.multitenancy.TenantFilterApplicator;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link org.springframework.boot.test.context.SpringBootTest} + H2：验证 Hibernate {@code @Filter} 在
 * {@link org.springframework.transaction.annotation.Transactional} 边界由切面绑定后，对 派生查询 / JPQL
 * select（含多表 join、fetch join）的隔离；bulk update/delete 与原生 SQL 须在语句中显式带租户条件。
 */
@SpringBootTest(classes = MultitenancyItTestApplication.class, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("mt-it")
@Transactional
class MultitenancyH2IntegrationTest {

  private static final long TENANT_A = 100L;
  private static final long TENANT_B = 200L;

  @Autowired
  private MtItDepartmentRepository departmentRepository;
  @Autowired
  private MtItEmployeeRepository employeeRepository;
  @PersistenceContext
  private EntityManager entityManager;

  @BeforeEach
  @AfterEach
  void clearPrincipal() {
    PrincipalContext.remove();
  }

  private void bindApiTenant(long tenantId) {
    Principal p = new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(tenantId)
        .setOptTenantId(null);
    PrincipalContext.set(p);
    TenantFilterApplicator.syncSession(entityManager.unwrap(Session.class));
  }

  @Test
  void findAll_and_derivedQuery_respectTenantBoundary() {
    bindApiTenant(TENANT_A);
    MtItDepartment deptA = departmentRepository.save(new MtItDepartment().setName("Sales"));
    employeeRepository.save(new MtItEmployee().setName("Alice").setDepartment(deptA));

    bindApiTenant(TENANT_B);
    MtItDepartment deptB = departmentRepository.save(new MtItDepartment().setName("RnD"));
    employeeRepository.save(new MtItEmployee().setName("Bob").setDepartment(deptB));

    bindApiTenant(TENANT_A);
    entityManager.flush();
    entityManager.clear();
    assertThat(employeeRepository.findAll()).extracting(MtItEmployee::getName)
        .containsExactly("Alice");
    assertThat(employeeRepository.findByDepartment_Name("Sales")).hasSize(1);
    assertThat(employeeRepository.findByDepartment_Name("RnD")).isEmpty();

    bindApiTenant(TENANT_B);
    entityManager.flush();
    entityManager.clear();
    assertThat(employeeRepository.findAll()).extracting(MtItEmployee::getName)
        .containsExactly("Bob");
  }

  @Test
  void jpqlJoinFetch_and_countJoin_onlySeeCurrentTenant() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = departmentRepository.save(new MtItDepartment().setName("DeptA"));
    employeeRepository.save(new MtItEmployee().setName("E1").setDepartment(d));

    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("DeptB"));
    employeeRepository.save(new MtItEmployee().setName("E2").setDepartment(d2));

    bindApiTenant(TENANT_A);
    entityManager.flush();
    entityManager.clear();
    assertThat(employeeRepository.findAllByDepartmentNameJoinFetch("DeptA")).hasSize(1);
    assertThat(employeeRepository.countByDepartmentName("DeptA")).isEqualTo(1L);
    assertThat(employeeRepository.countByDepartmentName("DeptB")).isZero();

    entityManager.flush();
    entityManager.clear();

    assertThat(departmentRepository.findDepartmentsByEmployeeNameContaining("E1")).hasSize(1);
    assertThat(departmentRepository.findDepartmentsByEmployeeNameContaining("E2")).isEmpty();
  }

  @Test
  void jpqlFetchJoin_loadsEmployeesForCurrentTenantOnly() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = new MtItDepartment().setName("WithEmps");
    MtItEmployee e1 = new MtItEmployee().setName("A1").setDepartment(d);
    d.getEmployees().add(e1);
    departmentRepository.save(d);

    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("WithEmps"));
    employeeRepository.save(new MtItEmployee().setName("B1").setDepartment(d2));

    bindApiTenant(TENANT_A);
    entityManager.flush();
    entityManager.clear();
    MtItDepartment loaded =
        departmentRepository.findWithEmployeesByName("WithEmps", TENANT_A).orElseThrow();
    assertThat(loaded.getEmployees()).hasSize(1);
    assertThat(loaded.getEmployees().get(0).getName()).isEqualTo("A1");
  }

  @Test
  void jpqlUpdate_onlyTouchesRowsInCurrentTenant() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = departmentRepository.save(new MtItDepartment().setName("D"));
    employeeRepository.save(new MtItEmployee().setName("SameName").setDepartment(d));

    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("D2"));
    employeeRepository.save(new MtItEmployee().setName("SameName").setDepartment(d2));

    bindApiTenant(TENANT_A);
    int updated = employeeRepository.updateNameJpql("SameName", "RenamedA", TENANT_A);
    assertThat(updated).isEqualTo(1);

    bindApiTenant(TENANT_B);
    assertThat(employeeRepository.findByDepartment_Name("D2").get(0).getName()).isEqualTo(
        "SameName");

    bindApiTenant(TENANT_A);
    assertThat(employeeRepository.findByDepartment_Name("D").get(0).getName()).isEqualTo(
        "RenamedA");
  }

  @Test
  void specificationQuery_appliesTenantFilter() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = departmentRepository.save(new MtItDepartment().setName("S"));
    employeeRepository.save(new MtItEmployee().setName("Charlie").setDepartment(d));

    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("S2"));
    employeeRepository.save(new MtItEmployee().setName("Dave").setDepartment(d2));

    bindApiTenant(TENANT_A);
    Specification<MtItEmployee> nameLike =
        (root, q, cb) -> cb.like(root.get("name"), "%ar%");
    assertThat(employeeRepository.findAll(nameLike)).extracting(MtItEmployee::getName)
        .containsExactly("Charlie");
  }

  @Test
  void nativeQuery_withExplicitTenantId_returnsExpectedSlice() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = departmentRepository.save(new MtItDepartment().setName("NatA"));
    employeeRepository.save(new MtItEmployee().setName("Na").setDepartment(d));

    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("NatB"));
    employeeRepository.save(new MtItEmployee().setName("Nb").setDepartment(d2));

    assertThat(employeeRepository.listNamesNativeForTenant(TENANT_A)).containsExactly("Na");
    assertThat(employeeRepository.listNamesNativeForTenant(TENANT_B)).containsExactly("Nb");
    assertThat(departmentRepository.findNameNativeForTenant(TENANT_A, "NatA")).hasValue("NatA");
    assertThat(departmentRepository.findNameNativeForTenant(TENANT_B, "NatA")).isEmpty();
  }

  @Test
  void nativeQuery_withoutTenantClause_seesAllRows_demonstratesBypass() {
    bindApiTenant(TENANT_A);
    MtItDepartment d = departmentRepository.save(new MtItDepartment().setName("X"));
    employeeRepository.save(new MtItEmployee().setName("P1").setDepartment(d));
    bindApiTenant(TENANT_B);
    MtItDepartment d2 = departmentRepository.save(new MtItDepartment().setName("Y"));
    employeeRepository.save(new MtItEmployee().setName("P2").setDepartment(d2));

    entityManager.flush();
    Object raw = entityManager.createNativeQuery("select count(*) from mt_it_employee")
        .getSingleResult();
    assertThat(((Number) raw).longValue()).isEqualTo(2L);
  }

  @Test
  void syncSession_throwsWhenTenantRequiredButInvalid() {
    bindApiTenant(TENANT_A);
    departmentRepository.save(new MtItDepartment().setName("Z"));

    Principal bad = new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(0L)
        .setOptTenantId(null);
    PrincipalContext.set(bad);

    assertThatThrownBy(
        () -> TenantFilterApplicator.syncSession(entityManager.unwrap(Session.class)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("optTenantId");
  }
}
