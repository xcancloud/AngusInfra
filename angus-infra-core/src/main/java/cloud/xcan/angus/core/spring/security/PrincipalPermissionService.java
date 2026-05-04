package cloud.xcan.angus.core.spring.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import java.util.Objects;

/**
 * `@PreAuthorize` will depend on a secure context:
 *
 * <p>
 * Use `@PreAuthorize("@PPS.XXX")` by Unsafe interface(/innerapi or /pubapi) will throw an
 * exception: An authentication object was not found in the SecurityContext
 */
public class PrincipalPermissionService {

  public boolean hasAuthority(String authority) {
    return PrincipalContextUtils.isTenantSysAdmin()
        || PrincipalContextUtils.hasAuthority(authority);
  }

  public boolean hasAnyAuthority(String... authorities) {
    return PrincipalContextUtils.isTenantSysAdmin()
        || PrincipalContextUtils.hasAnyAuthority(authorities);
  }

  public boolean hasPolicy(String policy) {
    return PrincipalContextUtils.isTenantSysAdmin()
        || PrincipalContextUtils.hasPolicy(policy);
  }

  public boolean hasAnyPolicy(String... policies) {
    return PrincipalContextUtils.isTenantSysAdmin()
        || PrincipalContextUtils.hasAnyPolicy(policies);
  }

  /**
   * The system administrator has all permissions including TOP.
   */
  public boolean hasToPolicy(String policy) {
    return !PrincipalContextUtils.isOpMultiTenant() || isOpClient()
        || PrincipalContextUtils.isOpSysAdmin()
        || PrincipalContextUtils.hasToRole(policy);
  }

  /**
   * The system administrator has all permissions including TOP.
   */
  public boolean hasAnyToPolicy(String... policies) {
    return !PrincipalContextUtils.isOpMultiTenant() || isOpClient()
        || PrincipalContextUtils.isOpSysAdmin()
        || PrincipalContextUtils.hasAnyToRole(policies);
  }

  public boolean isSysAdmin() {
    return PrincipalContextUtils.isSysAdmin();
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
    ApplicationInfo app = getApplicationInfo();
    return app.isPrivateEdition()
        || (app.isCloudServiceEdition()
        && Objects.equals(getOptTenantId(), OWNER_TENANT_ID));
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only TOPolicy user and tenant
   * own.
   */
  public boolean checkCloudTenantOperationSecurity(Long ownerTenantId) {
    ApplicationInfo app = getApplicationInfo();
    return app.isPrivateEdition()
        || (app.isCloudServiceEdition() && Objects.equals(getOptTenantId(), ownerTenantId));
  }
}
