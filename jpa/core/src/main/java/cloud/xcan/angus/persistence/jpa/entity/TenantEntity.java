package cloud.xcan.angus.persistence.jpa.entity;

import cloud.xcan.angus.persistence.jpa.multitenancy.TenantAware;
import cloud.xcan.angus.persistence.jpa.multitenancy.TenantListener;
import cloud.xcan.angus.spec.experimental.Entity;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import cloud.xcan.angus.spec.experimental.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Filter;

@MultiTenant
@Filter(
    name = "xcanTenantScope",
    deduceAliasInjectionPoints = false,
    condition = "{alias}.tenant_id = :tenantId")
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
