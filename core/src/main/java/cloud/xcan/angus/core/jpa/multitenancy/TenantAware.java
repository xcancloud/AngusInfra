package cloud.xcan.angus.core.jpa.multitenancy;

public interface TenantAware {

  Object setTenantId(Long tenantId);

  Long getTenantId();

}
