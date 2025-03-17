package cloud.xcan.sdf.core.biz;

import static cloud.xcan.sdf.api.message.http.Forbidden.M.DENIED_OP_TENANT_ACCESS_T;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.FATAL_EXIT_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_ADMIN_PERMISSION_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_CLOUD_SERVICE_EDITION_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_OP_APP_ADMIN_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_OP_CLIENT_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_OP_POLICY_PERMISSION_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_OP_POLICY_PERMISSION_T;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_OP_SYS_ADMIN_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_POLICY_PERMISSION_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_POLICY_PERMISSION_T;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_SYS_ADMIN_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TENANT_CLIENT_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TENANT_SYS_ADMIN_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TO_POLICY_PERMISSION_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TO_POLICY_PERMISSION_T;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TO_USER_PERMISSION;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.NO_TO_USER_PERMISSION_KEY;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.getRequiredToPolicy;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.hasAnyPolicy;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isOpClient;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isOpMultiTenant;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isOpSysAdmin;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isSysAdmin;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isTenantClient;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isToUser;
import static cloud.xcan.sdf.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static org.apache.commons.lang3.StringUtils.join;

import cloud.xcan.sdf.api.message.http.Forbidden;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;

public class PermissionCheck {

  /**
   * Check whether the (operation or tenant) system admin policy
   */
  public static void checkSysAdmin() {
    if (!isSysAdmin()) {
      throw Forbidden.of(NO_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the (operation or tenant) system admin policy
   */
  public static void checkSysAdmin(Principal principal) {
    if (!isSysAdmin(principal)) {
      throw Forbidden.of(NO_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the operation system admin policy
   */
  public static void checkOpSysAdmin() {
    if (!isOpSysAdmin()) {
      throw Forbidden.of(NO_OP_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  public static void checkMultiTenantPermission(Principal principal) {
    if (!principal.isMultiTenantCtrl()) {
      checkMultiTenantPermission0(principal);
    }
  }

  public static void checkMultiTenantPermission() {
    Principal principal = PrincipalContext.get();
    if (principal.isMultiTenantCtrl()) {
      checkMultiTenantPermission0(principal);
    }
  }

  public static void checkMultiTenantPermission0(Principal principal) {
    ProtocolAssert.assertForbidden(!isOpMultiTenant(principal)
            || (isOpClient(principal) && isToUser()), DENIED_OP_TENANT_ACCESS_T,
        new Object[]{String.valueOf(principal.getOptTenantId()), FATAL_EXIT_KEY});
  }

  public static void checkMultiTenantPermission0() {
    Principal principal = PrincipalContext.get();
    ProtocolAssert.assertForbidden(!isOpMultiTenant(principal)
            || (isOpClient(principal) && isToUser()), DENIED_OP_TENANT_ACCESS_T,
        new Object[]{String.valueOf(principal.getOptTenantId()), FATAL_EXIT_KEY});
  }

  /**
   * Check whether the operation system admin policy
   */
  public static void checkOpSysAdmin(Principal principal) {
    if (!isOpSysAdmin(principal)) {
      throw Forbidden.of(NO_OP_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the tenant system admin policy
   */
  public static void checkTenantSysAdmin() {
    if (!isTenantSysAdmin()) {
      throw Forbidden.of(NO_TENANT_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the tenant admin policy
   */
  public static void checkTenantSysAdmin(Principal principal) {
    if (!isTenantSysAdmin(principal)) {
      throw Forbidden.of(NO_TENANT_SYS_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the operation app admin policy
   */
  public static void checkOperationAppAdmin(String appAdminPolicyCode) {
    if (isOpClient() && !hasPolicy(appAdminPolicyCode)) {
      throw Forbidden.of(NO_OP_APP_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the tenant system admin policy
   */
  public static void checkTenantSysAdmin(String appAdminPolicyCode) {
    if (isTenantClient() && !hasPolicy(appAdminPolicyCode)) {
      throw Forbidden.of(NO_OP_APP_ADMIN_PERMISSION, NO_ADMIN_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the policy
   */
  public static void checkHasPolicy(String policyCode) {
    if (!hasPolicy(policyCode)) {
      throw Forbidden.of(NO_POLICY_PERMISSION_T, new Object[]{policyCode},
          NO_POLICY_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the any policy
   */
  public static void checkHasAnyPolicy(String... policyCodes) {
    if (!hasAnyPolicy(policyCodes)) {
      throw Forbidden.of(NO_POLICY_PERMISSION_T, new Object[]{join(policyCodes, ",")},
          NO_POLICY_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the operation policy
   */
  public static void checkHasOpPolicy(String policyCode) {
    if (!hasPolicy(policyCode)) {
      throw Forbidden.of(NO_OP_POLICY_PERMISSION_T, new Object[]{policyCode},
          NO_OP_POLICY_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the any operation policy
   */
  public static void checkHasAnyOpPolicy(String... policyCodes) {
    if (!hasAnyPolicy(policyCodes)) {
      throw Forbidden.of(NO_OP_POLICY_PERMISSION_T, new Object[]{join(policyCodes, ",")},
          NO_OP_POLICY_PERMISSION_KEY);
    }
  }

  public static void checkToPolicyUser() {
    if (!isToUser()) {
      throw Forbidden.of(NO_TO_POLICY_PERMISSION_T,
          new Object[]{getRequiredToPolicy()}, NO_TO_POLICY_PERMISSION_KEY);
    }
  }

  public static void checkToUserRequired() {
    if (!isToUser()) {
      throw Forbidden.of(NO_TO_USER_PERMISSION, NO_TO_USER_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the TOP policy
   */
  public static void checkHasToPolicy(String policyCode) {
    if (!hasPolicy(policyCode)) {
      throw Forbidden.of(NO_TO_POLICY_PERMISSION_T, new Object[]{policyCode},
          NO_TO_POLICY_PERMISSION_KEY);
    }
  }

  /**
   * Check whether the any TOP policy
   */
  public static void checkHasAnyToPolicy(String... policyCodes) {
    if (!hasAnyPolicy(policyCodes)) {
      throw Forbidden.of(NO_TO_POLICY_PERMISSION_T, new Object[]{join(policyCodes, ",")},
          NO_TO_POLICY_PERMISSION_KEY);
    }
  }

  public static void checkOpClient() {
    ProtocolAssert.assertForbidden(isOpClient(), NO_OP_CLIENT_PERMISSION);
  }

  public static void checkTenantClient() {
    ProtocolAssert.assertForbidden(isTenantClient(), NO_TENANT_CLIENT_PERMISSION);
  }

  public static void checkCloudServiceEdition() {
    ProtocolAssert.assertForbidden(isCloudServiceEdition(), NO_CLOUD_SERVICE_EDITION_PERMISSION);
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only us.
   */
  public static void checkCloudTenantSecurity() {
    ApplicationInfo app = getApplicationInfo();
    ProtocolAssert.assertForbidden(app.isPrivateEdition()
            || (app.isCloudServiceEdition() && getOptTenantId().equals(OWNER_TENANT_ID)),
        "User illegally access cloud edition data", FATAL_EXIT_KEY);
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only TOPolicy user and tenant
   * own.
   */
  public static void checkCloudTenantOperationSecurity(Long ownerTenantId) {
    ApplicationInfo app = getApplicationInfo();
    ProtocolAssert.assertForbidden(app.isPrivateEdition() || (app.isCloudServiceEdition()
            && (getOptTenantId().equals(ownerTenantId) || isToUser())),
        "User illegally access cloud edition data", FATAL_EXIT_KEY);
  }
}
