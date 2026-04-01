package cloud.xcan.angus.persistence.jpa.multitenancy.it;

import cloud.xcan.angus.core.jpa.entity.TenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 集成测试：多租户部门（与 {@link MtItEmployee} 一对多）。
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity(name = "MtItDepartment")
@Table(name = "mt_it_department")
public class MtItDepartment extends TenantEntity<MtItDepartment, Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @OneToMany(mappedBy = "department", cascade = CascadeType.PERSIST)
  private List<MtItEmployee> employees = new ArrayList<>();

  @Override
  public Long identity() {
    return id;
  }
}
