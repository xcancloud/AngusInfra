package cloud.xcan.angus.core.spring.security;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isOpMultiTenant;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;

/**
 * <p>
 * Principal Permission Service (PPS) provides centralized authorization and permission
 * checking capabilities for Spring Security's @PreAuthorize annotations. This service
 * encapsulates complex permission logic and multi-tenant security rules.
 * </p>
 * 
 * <p>
 * Key features:
 * - Role-based access control (RBAC) with hierarchical permissions
 * - Multi-tenant security with tenant isolation
 * - Platform and system administrator privilege management
 * - Policy-based authorization with flexible permission models
 * - Cloud service edition security controls
 * - Integration with Spring Security's expression language
 * </p>
 * 
 * <p>
 * Usage in Spring Security expressions:
 * <pre>
 * &#64;PreAuthorize("@PPS.hasAuthority('USER_READ')")
 * public User getUser(Long id) { ... }
 * 
 * &#64;PreAuthorize("@PPS.hasAnyAuthority('USER_READ', 'USER_WRITE')")
 * public List&lt;User&gt; getUsers() { ... }
 * 
 * &#64;PreAuthorize("@PPS.isCloudTenantSecurity()")
 * public void adminOperation() { ... }
 * </pre>
 * </p>
 * 
 * <p>
 * Security Context Requirement: This service depends on a valid Spring Security context.
 * Using @PreAuthorize with PPS methods on unsafe interfaces (/innerapi or /pubapi)
 * will throw "An authentication object was not found in the SecurityContext" exception.
 * </p>
 * 
 * <p>
 * Permission Hierarchy:
 * - Platform System Admin: Full access across all platforms and tenants
 * - Tenant System Admin: Full access within their tenant scope
 * - Operation System Admin: Cross-tenant operations in multi-tenant mode
 * - Regular Users: Limited access based on assigned authorities and policies
 * </p>
 * 
 * @see PrincipalContextUtils
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
public class PrincipalPermissionService {

  /* ==================== Administrative Role Checks ==================== */

  /**
   * <p>
   * Checks if the current principal is a platform system administrator
   * for the specified platform code.
   * </p>
   * 
   * <p>
   * Platform system administrators have the highest level of access
   * and can perform operations across all tenants and platforms.
   * </p>
   *
   * @param platformCode the platform code to check administrative access for
   * @return true if the principal is a platform system administrator
   */
  private boolean isPlatformSysAdmin(String platformCode) {
    return PrincipalContextUtils.isPlatformSysAdmin(platformCode);
  }

  /**
   * <p>
   * Checks if the current principal is a tenant system administrator.
   * Tenant system administrators have full access within their tenant scope.
   * </p>
   *
   * @return true if the principal is a tenant system administrator
   */
  private boolean isTenantSysAdmin() {
    return PrincipalContextUtils.isTenantSysAdmin();
  }

  /**
   * <p>
   * Checks if the current principal is an operation system administrator.
   * Operation system administrators can perform cross-tenant operations
   * in multi-tenant environments.
   * </p>
   *
   * @return true if the principal is an operation system administrator
   */
  private boolean isOpSysAdmin() {
    return PrincipalContextUtils.isOpSysAdmin();
  }

  /* ==================== Authority-Based Permissions ==================== */

  /**
   * <p>
   * Checks if the current principal has the specified authority.
   * Tenant system administrators automatically have all authorities within their scope.
   * </p>
   * 
   * <p>
   * This method is commonly used for fine-grained permission checks
   * where specific authorities are required for operations.
   * </p>
   *
   * @param authority the authority to check
   * @return true if the principal has the authority or is a tenant system administrator
   */
  public boolean hasAuthority(String authority) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAuthority(authority);
  }

  /**
   * <p>
   * Checks if the current principal has any of the specified authorities.
   * Tenant system administrators automatically pass this check.
   * </p>
   * 
   * <p>
   * This method is useful for operations that can be performed by users
   * with different but equivalent authorities.
   * </p>
   *
   * @param authorities the authorities to check (any one is sufficient)
   * @return true if the principal has any of the authorities or is a tenant system administrator
   */
  public boolean hasAnyAuthority(String... authorities) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAnyAuthority(authorities);
  }

  /* ==================== Policy-Based Permissions ==================== */

  /**
   * <p>
   * Checks if the current principal has the specified policy permission.
   * Policies represent higher-level business permissions that may encompass
   * multiple authorities.
   * </p>
   *
   * @param policy the policy to check
   * @return true if the principal has the policy or is a tenant system administrator
   */
  public boolean hasPolicy(String policy) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAuthority(policy);
  }

  /**
   * <p>
   * Checks if the current principal has any of the specified policy permissions.
   * </p>
   *
   * @param policies the policies to check (any one is sufficient)
   * @return true if the principal has any of the policies or is a tenant system administrator
   */
  public boolean hasAnyPolicy(String... policies) {
    return isTenantSysAdmin() || PrincipalContextUtils.hasAnyPolicy(policies);
  }

  /**
   * <p>
   * Checks if the current principal has the specified TOP (Tenant Operation Policy).
   * TOP policies are special permissions that include system administrator privileges.
   * </p>
   * 
   * <p>
   * This method provides enhanced security for cross-tenant operations:
   * - In single-tenant mode: always returns true
   * - In multi-tenant mode: checks for operation client, system admin, or specific TO role
   * </p>
   *
   * @param policy the TOP policy to check
   * @return true if the principal has the TOP policy or sufficient administrative privileges
   */
  public boolean hasToPolicy(String policy) {
    return !isOpMultiTenant() || isOpClient() || isOpSysAdmin()
        || PrincipalContextUtils.hasToRole(policy);
  }

  /**
   * <p>
   * Checks if the current principal has any of the specified TOP policies.
   * </p>
   *
   * @param policies the TOP policies to check (any one is sufficient)
   * @return true if the principal has any TOP policy or sufficient administrative privileges
   */
  public boolean hasAnyToPolicy(String... policies) {
    return !isOpMultiTenant() || isOpClient() || isOpSysAdmin()
        || PrincipalContextUtils.hasAnyToRole(policies);
  }

  /* ==================== Client Type Checks ==================== */

  /**
   * <p>
   * Checks if the current principal represents an operation client.
   * Operation clients have special privileges for system-level operations.
   * </p>
   *
   * @return true if the principal is an operation client
   */
  public boolean isOpClient() {
    return PrincipalContextUtils.isOpClient();
  }

  /**
   * <p>
   * Checks if the current principal represents a tenant client.
   * Tenant clients operate within the scope of a specific tenant.
   * </p>
   *
   * @return true if the principal is a tenant client
   */
  public boolean isTenantClient() {
    return PrincipalContextUtils.isTenantClient();
  }

  /* ==================== Edition and Deployment Checks ==================== */

  /**
   * <p>
   * Checks if the application is running in cloud service edition mode.
   * Cloud service edition has specific security and multi-tenancy requirements.
   * </p>
   *
   * @return true if running in cloud service edition
   */
  public boolean isCloudServiceEdition() {
    return PrincipalContextUtils.isCloudServiceEdition();
  }

  /**
   * <p>
   * Ensures cloud tenant security by restricting operations to authorized contexts.
   * This method is critical for maintaining data isolation in cloud deployments.
   * </p>
   * 
   * <p>
   * Security rules:
   * - Private edition: always allows operation (single-tenant)
   * - Cloud service edition: only allows operations by the owner tenant
   * </p>
   * 
   * <p>
   * Use this method to protect sensitive operations that should only be
   * performed by the platform owner in cloud service deployments.
   * </p>
   *
   * @return true if the operation is allowed under cloud tenant security rules
   */
  public boolean isCloudTenantSecurity() {
    return getApplicationInfo().isPrivateEdition() ||
        (getApplicationInfo().isCloudServiceEdition() && getOptTenantId().equals(OWNER_TENANT_ID));
  }

  /**
   * <p>
   * Checks cloud tenant operation security for a specific tenant owner.
   * This method validates that the current principal can perform operations
   * on resources owned by the specified tenant.
   * </p>
   * 
   * <p>
   * Security validation:
   * - Private edition: always allows (no multi-tenancy)
   * - Cloud service edition: allows if current tenant matches owner or user has TO privileges
   * </p>
   * 
   * <p>
   * This method is essential for maintaining proper tenant isolation
   * while allowing authorized cross-tenant operations.
   * </p>
   *
   * @param ownerTenantId the ID of the tenant that owns the resource
   * @return true if the operation is authorized for the specified tenant
   */
  public boolean checkCloudTenantOperationSecurity(Long ownerTenantId) {
    ApplicationInfo app = getApplicationInfo();
    return app.isPrivateEdition() || (app.isCloudServiceEdition()
        && (getOptTenantId().equals(ownerTenantId) || PrincipalContextUtils.isToUser()));
  }
}
