package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import cloud.xcan.angus.persistence.jpa.multitenancy.TenantEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 集成测试：多租户员工，关联部门（多表路径）。
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity(name = "MtItEmployee")
@Table(name = "mt_it_employee")
public class MtItEmployee extends TenantEntity<MtItEmployee, Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private MtItDepartment department;

  @Override
  public Long identity() {
    return id;
  }
}
