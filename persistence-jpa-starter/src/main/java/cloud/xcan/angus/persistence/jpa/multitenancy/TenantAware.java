package cloud.xcan.angus.persistence.jpa.multitenancy;

public interface TenantAware {

  Object setTenantId(Long tenantId);

  Long getTenantId();

}
