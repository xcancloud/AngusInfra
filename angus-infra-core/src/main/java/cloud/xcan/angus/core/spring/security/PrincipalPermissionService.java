package cloud.xcan.angus.core.spring.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.spec.principal.PrincipalContext;
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
    return tenantAdminAuthorityBypass()
        || PrincipalContextUtils.hasAuthority(authority);
  }

  public boolean hasAnyAuthority(String... authorities) {
    return tenantAdminAuthorityBypass()
        || PrincipalContextUtils.hasAnyAuthority(authorities);
  }

  public boolean hasPolicy(String policy) {
    return tenantAdminAuthorityBypass()
        || PrincipalContextUtils.hasPolicy(policy);
  }

  public boolean hasAnyPolicy(String... policies) {
    return tenantAdminAuthorityBypass()
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

  /**
   * 会话租户管理员短路全权限；PAT（{@code isUserToken}）必须按令牌 permissions 校验。
   */
  private boolean tenantAdminAuthorityBypass() {
    if (!PrincipalContextUtils.isTenantSysAdmin()) {
      return false;
    }
    try {
      return !PrincipalContext.get().isUserToken();
    } catch (Exception e) {
      return true;
    }
  }
}
