package cloud.xcan.angus.core.spring.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isOpMultiTenant;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;

/**
 * `@PreAuthorize` will depend on a secure context:
 *
 * <p>
 * Use `@PreAuthorize("@PPS.XXX")` by Unsafe interface(/doorapi or /pubapi) will throw an exception:
 * An authentication object was not found in the SecurityContext
 */
public class PrincipalPermissionService {

  private boolean isPlatformSysAdmin(String platformCode) {
    return PrincipalContextUtils.isPlatformSysAdmin(platformCode);
  }

  private boolean isTenantSysAdmin() {
    return PrincipalContextUtils.isTenantSysAdmin();
  }

  private boolean isOpSysAdmin() {
    return PrincipalContextUtils.isOpSysAdmin();
  }

  public boolean hasAuthority(String authority) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAuthority(authority);
  }

  public boolean hasAnyAuthority(String... authorities) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAnyAuthority(authorities);
  }

  public boolean hasPolicy(String policy) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAuthority(policy);
  }

  public boolean hasAnyPolicy(String... policies) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAnyPolicy(policies);
  }

  /**
   * The system administrator has all permissions including TOP.
   */
  public boolean hasToPolicy(String policy) {
    return !isOpMultiTenant() || isOpClient() || isOpSysAdmin()
        || PrincipalContextUtils.hasToPolicy(policy);
  }

  /**
   * The system administrator has all permissions including TOP.
   */
  public boolean hasAnyToPolicy(String... policies) {
    return !isOpMultiTenant() || isOpClient() || isOpSysAdmin()
        || PrincipalContextUtils.hasAnyToPolicy(policies);
  }

  public boolean isOpClient() {
    return PrincipalContextUtils.isOpClient();
  }

  public boolean isTenantClient() {
    return PrincipalContextUtils.isTenantClient();
  }

  public boolean isCloudServiceEdition() {
    return PrincipalContextUtils.isCloudServiceEdition();
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only us.
   */
  public boolean isCloudTenantSecurity() {
    return getApplicationInfo().isPrivateEdition() ||
        (getApplicationInfo().isCloudServiceEdition() && getOptTenantId().equals(OWNER_TENANT_ID));
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only TOPolicy user and tenant
   * own.
   */
  public boolean checkCloudTenantOperationSecurity(Long ownerTenantId) {
    ApplicationInfo app = getApplicationInfo();
    return app.isPrivateEdition() || (app.isCloudServiceEdition()
        && (getOptTenantId().equals(ownerTenantId) || PrincipalContextUtils.isToUser()));
  }
}
