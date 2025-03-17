
package cloud.xcan.sdf.core.utils;


import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.POLICY_OP_PREFIX;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.POLICY_PREFIX;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.POLICY_TOP_PREFIX;
import static cloud.xcan.sdf.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.XCAN_OPERATION_PLATFORM_CODE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.sdf.spec.principal.Principal.DEFAULT_CLIENT_ID;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.api.enums.ApiType;
import cloud.xcan.sdf.api.enums.EditionType;
import cloud.xcan.sdf.core.biz.PermissionCheck;
import cloud.xcan.sdf.core.spring.SpringContextHolder;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.spec.experimental.BizConstant;
import cloud.xcan.sdf.spec.http.HttpMethod;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PrincipalContextUtils {

  public static Long getOptTenantId() {
    return getOptTenantId(PrincipalContext.get());
  }

  public static Long getOptTenantId(Principal principal) {
    if (isNull(principal.getOptTenantId())) {
      return principal.getTenantId();
    }
    if (!principal.isMultiTenantCtrl() || isJobOrDoorApi()
        || (isOpClient(principal) && isToUser())) {
      return principal.getOptTenantId();
    }
    return principal.getTenantId();
  }

  public static boolean hasOptTenantId() {
    return isValidOptTenantId(getOptTenantId());
  }

  public static Long getRealOptTenantId() {
    return getRealOptTenantId(PrincipalContext.get());
  }

  /**
   * Return null when optTenantId is not set
   */
  public static Long getRealOptTenantId(Principal principal) {
    if (isOpClient(principal) && isToUser()) {
      return principal.getOptTenantId();
    }
    return principal.getTenantId();
  }

  public static boolean hasRealOptTenantId() {
    return isValidOptTenantId(getRealOptTenantId());
  }

  public static Long getOriginalOptTenantId() {
    return getOriginalOptTenantId(PrincipalContext.get());
  }

  /**
   * Return null when optTenantId is not set
   */
  public static Long getOriginalOptTenantId(Principal principal) {
    return principal.getOptTenantId();
  }

  public static boolean hasOriginalOptTenantId() {
    return isValidOptTenantId(getOriginalOptTenantId());
  }

  public static boolean isValidOptTenantId(Long optTenantId) {
    return optTenantId != null && optTenantId >= OWNER_TENANT_ID;
  }

  public static void setOptTenantId(Long optTenantId) {
    PrincipalContext.get().setOptTenantId(optTenantId);
  }

  /**
   * BizTemplate multi tenant ctrl
   *
   * @see PrincipalContextUtils#isMultiTenantCtrl(Principal)
   */
  public static boolean isMultiTenantCtrl() {
    return PrincipalContext.get().isMultiTenantCtrl();
  }

  /**
   * BizTemplate multi tenant ctrl
   *
   * @see PrincipalContextUtils#isMultiTenantCtrl()
   */
  public static boolean isMultiTenantCtrl(Principal principal) {
    return principal.isMultiTenantCtrl();
  }

  public static void setMultiTenantCtrl(boolean multiTenantCtrl) {
    PrincipalContext.get().setMultiTenantCtrl(multiTenantCtrl);
  }

  public static String[] getRequiredToPolicy() {
    return PrincipalContext.get().getRequiredToPolicy();
  }

  public static String[] getRequiredToPolicy(Principal principal) {
    return principal.getRequiredToPolicy();
  }

  public static void setRequiredToPolicy(String... toPolicy) {
    PrincipalContext.get().setRequiredToPolicy(toPolicy);
  }

  /**
   * Check if the operation client is visiting
   */
  public static boolean isOpClient() {
    return XCAN_OPERATION_PLATFORM_CODE.equals(PrincipalContext.getClientId());
  }

  /**
   * Check if the operation client is visiting
   */
  public static boolean isOpClient(Principal principal) {
    return XCAN_OPERATION_PLATFORM_CODE.equals(principal.getClientId());
  }

  /**
   * Check if the tenant client is visiting
   */
  public static boolean isTenantClient() {
    return XCAN_TENANT_PLATFORM_CODE.equals(PrincipalContext.getClientId());
  }

  /**
   * Check if the tenant client is visiting
   */
  public static boolean isTenantClient(Principal principal) {
    return XCAN_TENANT_PLATFORM_CODE.equals(principal.getClientId());
  }

  /**
   * Check whether the (operation or tenant) system administrator
   */
  public static boolean isSysAdmin() {
    Principal principal = PrincipalContext.get();
    return principal.isAuthPassed() && principal.isSysAdminFlag();
  }

  /**
   * Check whether the platform system administrator
   */
  public static boolean isSysAdmin(Principal principal) {
    return principal.isAuthPassed() && principal.isSysAdminFlag();
  }

  /**
   * Check whether the operation system admin policy
   */
  public static boolean isOpSysAdmin() {
    return isPlatformSysAdmin(XCAN_OPERATION_PLATFORM_CODE);
  }

  public static boolean isToUser() {
    Principal principal = PrincipalContext.get();
    return isOpSysAdmin(principal) || (isToUser0(principal) && (
        isEmpty(principal.getRequiredToPolicy()) || hasAnyToPolicy(principal.getRequiredToPolicy())
    ));
  }

  public static boolean isToUser0() {
    return isToUser0(PrincipalContext.get());
  }

  public static boolean isToUser0(Principal principal) {
    return principal.isAuthPassed() && principal.isToUserFlag() && isOpClient(principal);
  }

  public static boolean hasMultiTenantPermission() {
    Principal principal = PrincipalContext.get();
    return !isOpMultiTenant(principal) || (isOpClient(principal) && isToUser());
  }

  public static boolean hasMultiTenantPermission(Principal principal) {
    return !isOpMultiTenant(principal) || (isOpClient(principal) && isToUser());
  }

  public static boolean isTenantClientOpMultiTenant(Principal principal) {
    return isTenantClient(principal) && isOpMultiTenant(principal);
  }

  public static boolean isOpClientOpMultiTenant(Principal principal) {
    return isOpClient(principal) && isOpMultiTenant(principal);
  }

  public static boolean isOpMultiTenant(Principal principal) {
    return Objects.nonNull(principal.getOptTenantId()) && !principal.getOptTenantId()
        .equals(principal.getTenantId());
  }

  public static boolean isOpMultiTenant() {
    Principal principal = PrincipalContext.get();
    return Objects.nonNull(principal.getOptTenantId()) && !principal.getOptTenantId()
        .equals(principal.getTenantId());
  }

  /**
   * Check whether the operation system admin policy
   */
  public static boolean isOpSysAdmin(Principal principal) {
    return isPlatformSysAdmin(XCAN_OPERATION_PLATFORM_CODE, principal);
  }

  /**
   * Check whether the tenant system administrator
   */
  public static boolean isTenantSysAdmin() {
    return isPlatformSysAdmin(XCAN_TENANT_PLATFORM_CODE);
  }

  public static boolean isUserAction() {
    return isApi();
  }

  /**
   * Decide multi-tenant control switch by apiType
   *
   * @see PrincipalContextUtils#isMultiTenantCtrl()
   * @see PrincipalContextUtils#isMultiTenantCtrl(Principal)
   */
  public static boolean decideMultiTenantCtrlByApiType() {
    return isApi() || isOpenApi() || isOpenApi2p();
  }

  /**
   * Decide multi-tenant control switch by apiType
   *
   * @see PrincipalContextUtils#isMultiTenantCtrl()
   * @see PrincipalContextUtils#isMultiTenantCtrl(Principal)
   */
  public static boolean decideMultiTenantCtrlByApiType(Principal principal) {
    return isValidOptTenantId(getOriginalOptTenantId())
        || isApi(principal) || isOpenApi(principal) || isOpenApi2p(principal);
  }

  /**
   * Check whether the platform administrator
   *
   * @param platformCode platform code
   * @see BizConstant#XCAN_OPERATION_PLATFORM_CODE
   * @see BizConstant#XCAN_TENANT_PLATFORM_CODE
   */
  public static boolean isPlatformSysAdmin(String platformCode) {
    Principal principal = PrincipalContext.get();
    return principal.isAuthPassed() && principal.getClientId().equals(platformCode)
        && principal.isSysAdminFlag();
  }

  /**
   * Check whether the platform administrator
   *
   * @param platformCode platform code
   * @see BizConstant#XCAN_OPERATION_PLATFORM_CODE
   * @see BizConstant#XCAN_TENANT_PLATFORM_CODE
   */
  public static boolean isPlatformSysAdmin(String platformCode, Principal principal) {
    return principal.isAuthPassed() && principal.getClientId().equals(platformCode)
        && principal.isSysAdminFlag();
  }

  /**
   * Check whether the anonymous user
   */
  public static boolean isAnonymousUser() {
    return !PrincipalContext.isAuthPassed();
  }

  /**
   * Check whether the anonymous user
   */
  public static boolean isAnonymousUser(Principal principal) {
    return !principal.isAuthPassed();
  }

  public static boolean hasAuthority(String authority) {
    if (nonePolicyAndAuthority(authority)) {
      return false;
    }
    List<String> authorities = PrincipalContext.get().getAuthorities();
    for (String p : authorities) {
      if (authority.equals(p)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnyAuthority(String... authorities) {
    if (nonePolicyAndAuthority(authorities)) {
      return false;
    }
    List<String> authorities_ = PrincipalContext.get().getAuthorities();
    for (String authority : authorities) {
      for (String p : authorities_) {
        if (authority.equals(p)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasPolicy(String policy) {
    if (nonePolicyAndAuthority(policy)) {
      return false;
    }
    if (!policy.startsWith(POLICY_PREFIX)) {
      policy = POLICY_PREFIX + policy;
    }
    for (String p : PrincipalContext.get().getAuthorities()) {
      if (policy.equals(p)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnyPolicy(String... policies) {
    if (nonePolicyAndAuthority(policies)) {
      return false;
    }
    List<String> allPolicies = PrincipalContext.get().getAuthorities();
    for (String policy : policies) {
      if (!policy.startsWith(POLICY_PREFIX)) {
        policy = POLICY_PREFIX + policy;
      }
      for (String p : allPolicies) {
        if (policy.equals(p)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasOpPolicy(String policy) {
    if (nonePolicyAndAuthority(policy)) {
      return false;
    }
    if (!policy.startsWith(POLICY_OP_PREFIX)) {
      policy = POLICY_OP_PREFIX + policy;
    }
    for (String p : PrincipalContext.get().getAuthorities()) {
      if (policy.equals(p)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnyOpPolicy(String... policies) {
    if (nonePolicyAndAuthority(policies)) {
      return false;
    }
    List<String> allPolicies = PrincipalContext.get().getAuthorities();
    for (String policy : policies) {
      if (!policy.startsWith(POLICY_OP_PREFIX)) {
        policy = POLICY_OP_PREFIX + policy;
      }
      for (String p : allPolicies) {
        if (policy.equals(p)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasToPolicy(String policy) {
    if (nonePolicyAndAuthority(policy)) {
      return false;
    }
    if (!policy.startsWith(POLICY_TOP_PREFIX)) {
      policy = POLICY_TOP_PREFIX + policy;
    }
    for (String p : PrincipalContext.get().getAuthorities()) {
      if (policy.equals(p)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnyToPolicy(String... policies) {
    if (nonePolicyAndAuthority(policies)) {
      return false;
    }
    List<String> allPolicies = PrincipalContext.get().getAuthorities();
    for (String policy : policies) {
      if (!policy.startsWith(POLICY_TOP_PREFIX)) {
        policy = POLICY_TOP_PREFIX + policy;
      }
      for (String p : allPolicies) {
        if (policy.equals(p)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean nonePolicyAndAuthority(String... policyOrAuthority) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || isEmpty(policyOrAuthority)) {
      return true;
    }
    return isEmpty(PrincipalContext.get().getAuthorities());
  }


  /**
   * Check whether the (operation or tenant) system admin policy
   */
  public static void checkSysAdmin() {
    PermissionCheck.checkSysAdmin();
  }

  /**
   * Check whether the (operation or tenant) system admin policy
   */
  public static void checkSysAdmin(Principal principal) {
    PermissionCheck.checkSysAdmin(principal);
  }

  public static void checkToUserRequired() {
    PermissionCheck.checkToUserRequired();
  }

  /**
   * Check whether the operation system admin policy
   */
  public static void checkOpSysAdmin() {
    PermissionCheck.checkOpSysAdmin();
  }

  public static void checkMultiTenantPermission(Principal principal) {
    PermissionCheck.checkMultiTenantPermission(principal);
  }

  public static void checkMultiTenantPermission() {
    PermissionCheck.checkMultiTenantPermission();
  }

  public static void checkMultiTenantPermission0(Principal principal) {
    PermissionCheck.checkMultiTenantPermission0(principal);
  }

  public static void checkMultiTenantPermission0() {
    PermissionCheck.checkMultiTenantPermission0();
  }

  /**
   * Check whether the operation app admin policy
   */
  public static void checkOperationAppAdmin(String appAdminPolicyCode) {
    PermissionCheck.checkOperationAppAdmin(appAdminPolicyCode);
  }

  /**
   * Check whether the tenant system admin policy
   */
  public static void checkTenantSysAdmin(String appAdminPolicyCode) {
    PermissionCheck.checkTenantSysAdmin(appAdminPolicyCode);
  }

  /**
   * Check whether the operation system admin policy
   */
  public static void checkOpSysAdmin(Principal principal) {
    PermissionCheck.checkOpSysAdmin(principal);
  }

  /**
   * Check whether the tenant system admin policy
   */
  public static void checkTenantSysAdmin() {
    PermissionCheck.checkTenantSysAdmin();
  }

  /**
   * Check whether the tenant administrator
   */
  public static boolean isTenantSysAdmin(Principal principal) {
    return isPlatformSysAdmin(XCAN_TENANT_PLATFORM_CODE, principal);
  }

  /**
   * Check whether the tenant admin policy
   */
  public static void checkTenantSysAdmin(Principal principal) {
    PermissionCheck.checkTenantSysAdmin(principal);
  }

  /**
   * Check whether the policy
   */
  public static void checkHasPolicy(String policyCode) {
    PermissionCheck.checkHasPolicy(policyCode);
  }

  /**
   * Check whether the any policy
   */
  public static void checkHasAnyPolicy(String... policyCodes) {
    PermissionCheck.checkHasAnyPolicy(policyCodes);
  }

  /**
   * Check whether the operation policy
   */
  public static void checkHasOpPolicy(String policyCode) {
    PermissionCheck.checkHasOpPolicy(policyCode);
  }

  /**
   * Check whether the any operation policy
   */
  public static void checkHasAnyOpPolicy(String... policyCodes) {
    PermissionCheck.checkHasAnyOpPolicy(policyCodes);
  }

  public static void checkToPolicyUser() {
    PermissionCheck.checkToPolicyUser();
  }

  /**
   * Check whether the TOP policy
   */
  public static void checkHasToPolicy(String policyCode) {
    PermissionCheck.checkHasToPolicy(policyCode);
  }

  /**
   * Check whether the any TOP policy
   */
  public static void checkHasAnyToPolicy(String... policyCodes) {
    PermissionCheck.checkHasAnyToPolicy(policyCodes);
  }

  public static void checkOpClient() {
    PermissionCheck.checkOpClient();
  }

  public static void checkTenantClient() {
    PermissionCheck.checkTenantClient();
  }

  public static void checkCloudServiceEdition() {
    PermissionCheck.checkCloudServiceEdition();
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only us.
   */
  public static void checkCloudTenantSecurity() {
    PermissionCheck.checkCloudTenantSecurity();
  }

  /**
   * Not multi-tenant operation, ensuring cloud service security. Only TOPolicy user and tenant
   * own.
   */
  public static void checkCloudTenantOperationSecurity(Long ownerTenantId) {
    PermissionCheck.checkCloudTenantOperationSecurity(ownerTenantId);
  }

  public static boolean isPostRequest() {
    return HttpMethod.POST.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isPatchRequest() {
    return HttpMethod.PATCH.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isPutRequest() {
    return HttpMethod.PUT.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isDeleteRequest() {
    return HttpMethod.DELETE.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isGetRequest() {
    return HttpMethod.GET.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isCmdRequest() {
    return HttpMethod.POST.getValue().equals(PrincipalContext.getMethod())
        || HttpMethod.PUT.getValue().equals(PrincipalContext.getMethod())
        || HttpMethod.PATCH.getValue().equals(PrincipalContext.getMethod())
        || HttpMethod.DELETE.getValue().equals(PrincipalContext.getMethod());
  }

  public static boolean isPostRequest(Principal principal) {
    return HttpMethod.POST.getValue().equals(principal.getMethod());
  }

  public static boolean isPatchRequest(Principal principal) {
    return HttpMethod.PATCH.getValue().equals(principal.getMethod());
  }

  public static boolean isPutRequest(Principal principal) {
    return HttpMethod.PUT.getValue().equals(principal.getMethod());
  }

  public static boolean isDeleteRequest(Principal principal) {
    return HttpMethod.DELETE.getValue().equals(principal.getMethod());
  }

  public static boolean isGetRequest(Principal principal) {
    return HttpMethod.GET.getValue().equals(principal.getMethod());
  }

  public static boolean isCmdRequest(Principal principal) {
    return HttpMethod.POST.getValue().equals(principal.getMethod())
        || HttpMethod.PUT.getValue().equals(principal.getMethod())
        || HttpMethod.PATCH.getValue().equals(principal.getMethod())
        || HttpMethod.DELETE.getValue().equals(principal.getMethod());
  }

  public static boolean isApi() {
    return ApiType.API.equals(PrincipalContext.getApiType());
  }

  public static boolean isApi(Principal principal) {
    return ApiType.API.equals(principal.getApiType());
  }

  public static boolean isOpenApi() {
    return ApiType.OPEN_API.equals(PrincipalContext.getApiType());
  }

  public static boolean isOpenApi(Principal principal) {
    return ApiType.OPEN_API.equals(principal.getApiType());
  }

  public static boolean isOpenApi2p() {
    return ApiType.OPEN_API_2P.equals(PrincipalContext.getApiType());
  }

  public static boolean isOpenApi2p(Principal principal) {
    return ApiType.OPEN_API_2P.equals(principal.getApiType());
  }

  public static boolean isView() {
    return ApiType.VIEW.equals(PrincipalContext.getApiType());
  }

  public static boolean isDoorApi() {
    return ApiType.DOOR_API.equals(PrincipalContext.getApiType());
  }

  public static boolean isDoorApi(Principal principal) {
    return ApiType.DOOR_API.equals(principal.getApiType());
  }

  public static boolean isPubApi() {
    return ApiType.PUB_API.equals(PrincipalContext.getApiType());
  }

  public static boolean isPubView() {
    return ApiType.PUB_VIEW.equals(PrincipalContext.getApiType());
  }

  public static boolean isJobOrDoorApi() {
    return isJob() || isDoorApi();
  }

  public static boolean isJobOrDoorApi(Principal principal) {
    return isJob(principal) || isDoorApi(principal);
  }

  public static boolean isJob() {
    return PrincipalContext.getApiType() == null && DEFAULT_CLIENT_ID.equals(
        PrincipalContext.getClientId());
  }

  public static boolean isJob(Principal principal) {
    return principal.getApiType() == null && DEFAULT_CLIENT_ID.equals(
        PrincipalContext.getClientId());
  }

  public static boolean isCloudServiceEdition() {
    return getApplicationInfo().isCloudServiceEdition();
  }

  public static boolean isPrivateEdition() {
    return getApplicationInfo().isPrivateEdition();
  }

  public static boolean isDatacenterEdition() {
    return getApplicationInfo().isDatacenterEdition();
  }

  public static boolean isEnterpriseEdition() {
    return getApplicationInfo().isEnterpriseEdition();
  }

  public static boolean isCommunityEdition() {
    return getApplicationInfo().isCommunityEdition();
  }

  public static boolean isEdition(EditionType editionType) {
    return getApplicationInfo().isEdition(editionType);
  }

  public static ApplicationInfo getApplicationInfo() {
    return SpringContextHolder.getBean(ApplicationInfo.class);
  }
}
