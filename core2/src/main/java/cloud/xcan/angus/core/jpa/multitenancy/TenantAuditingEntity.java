package cloud.xcan.angus.core.jpa.multitenancy;

import cloud.xcan.angus.core.jpa.auditor.AuditingEntity;
import cloud.xcan.angus.spec.experimental.Entity;
import cloud.xcan.angus.spec.experimental.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

@MultiTenant
@MappedSuperclass
@EntityListeners(TenantListener.class)
public abstract class TenantAuditingEntity<T extends Entity<T, ID>, ID> extends
    AuditingEntity<T, ID> implements TenantAware {

  @Column(name = "tenant_id")
  protected Long tenantId;

  public TenantAuditingEntity() {
  }

  public TenantAuditingEntity(Long tenantId) {
    this.tenantId = tenantId;
  }

  public Long getTenantId() {
    return tenantId;
  }

  public T setTenantId(Long tenantId) {
    this.tenantId = tenantId;
    return (T) this;
  }
}
