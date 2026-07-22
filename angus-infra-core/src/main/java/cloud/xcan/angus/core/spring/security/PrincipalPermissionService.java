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
    return sysAdminAuthorityBypass()
        || PrincipalContextUtils.hasAuthority(authority);
  }

  public boolean hasAnyAuthority(String... authorities) {
    return sysAdminAuthorityBypass()
        || PrincipalContextUtils.hasAnyAuthority(authorities);
  }

  public boolean hasPolicy(String policy) {
    return sysAdminAuthorityBypass()
        || PrincipalContextUtils.hasPolicy(policy);
  }

  public boolean hasAnyPolicy(String... policies) {
    return sysAdminAuthorityBypass()
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
   * 会话系统管理员短路全权限（覆盖租户端 xcan_tp 与运营端 xcan_op）。
   * <p>此前仅 {@code isTenantSysAdmin()}（要求 clientId=xcan_tp），导致拥有者租户管理员
   * 经运营端登录访问 OP 应用（如 AngusInsight）时 {@code @PPS.hasAuthority} 全部 403。
   * PAT（{@code isUserToken}）仍必须按令牌 permissions 校验。
   */
  private boolean sysAdminAuthorityBypass() {
    if (!PrincipalContextUtils.isSysAdmin()) {
      return false;
    }
    try {
      return !PrincipalContext.get().isUserToken();
    } catch (Exception e) {
      return true;
    }
  }
}
