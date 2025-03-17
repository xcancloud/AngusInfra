package cloud.xcan.sdf.core.jpa.multitenancy;

public interface TenantAware {

  Object setTenantId(Long tenantId);

  Long getTenantId();

}