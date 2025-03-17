package cloud.xcan.sdf.core.jpa.multitenancy;

import cloud.xcan.sdf.spec.experimental.Entity;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
import cloud.xcan.sdf.spec.experimental.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@MultiTenant
@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@EntityListeners(TenantListener.class)
public abstract class TenantEntity<T extends Entity<T, ID>, ID> extends
    EntitySupport<T, ID> implements TenantAware {

  @Column(name = "tenant_id")
  protected Long tenantId;

  public TenantEntity(Long tenantId) {
    this.tenantId = tenantId;
  }

}
