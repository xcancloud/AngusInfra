package cloud.xcan.angus.core.biz;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasOriginalOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isOpClient;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.setMultiTenantCtrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BizTemplate<T> {

  protected String bizKey0;
  protected boolean multiTenantCtrl = true;
  protected boolean autoCtrlWhenOpClient;
  private final String[] requiredToPolicy;

  protected BizTemplate() {
    this(true);
  }

  protected BizTemplate(String bizKey0) {
    this(bizKey0, true, false);
  }

  protected BizTemplate(boolean multiTenantCtrl) {
    this("", multiTenantCtrl, false);
  }

  protected BizTemplate(boolean multiTenantCtrl, String... requiredToPolicy) {
    this("", multiTenantCtrl, false, requiredToPolicy);
  }

  protected BizTemplate(boolean multiTenantCtrl, boolean autoCtrlWhenOpClient) {
    this("", multiTenantCtrl, autoCtrlWhenOpClient);
  }

  protected BizTemplate(boolean multiTenantCtrl, boolean autoCtrlWhenOpClient,
      String... requiredToPolicy) {
    this("", multiTenantCtrl, autoCtrlWhenOpClient, requiredToPolicy);
  }

  protected BizTemplate(String bizKey0, boolean multiTenantCtrl,
      boolean autoCtrlWhenOpClient, String... requiredTOPolicy) {
    this.bizKey0 = bizKey0;
    this.multiTenantCtrl = multiTenantCtrl;
    this.autoCtrlWhenOpClient = autoCtrlWhenOpClient && isOpClient();
    this.requiredToPolicy = requiredTOPolicy;
    Principal principal = PrincipalContext.get();

    if (isNotEmpty(requiredTOPolicy)) {
      // Used by TenantInterceptor
      principal.setRequiredToPolicy(requiredTOPolicy);
    }

    if (!multiTenantCtrl) {
      // Disable multi-tenancy control: Users must manually manage multi-tenant data isolation, including adding tenant ID conditions in SQL statements.
      principal.setMultiTenantCtrl(false);
    } else if (!isOpClient() ||
        (isUserAction() && !OWNER_TENANT_ID.equals(principal.getTenantId()))) {
      // Force enable multi-tenancy control: Multi-tenancy control is mandatory for non-operation client and tenant.
      principal.setMultiTenantCtrl(true);
    } else {
      // When the operation client doesn't specify a tenant, multi-tenancy control is disabled to allow querying data across all tenants.
      if (this.autoCtrlWhenOpClient) {
        principal.setMultiTenantCtrl(hasOriginalOptTenantId());
      }
    }
  }

  protected void checkParams() {
  }

  protected abstract T process();

  public T execute() {
    checkParams();
    checkPolicy();

    try {
      T result = process();
      onSuccess();
      return result;
    } catch (AbstractResultMessageException e) {
      log.info(bizKey0, e.toString());
      onCustomException(e);
      return null;
    } catch (RuntimeException e) {
      log.error(bizKey0, e);
      onException(e);
      return null;
    } catch (Throwable e) {
      log.error(bizKey0, e);
      throw e;
    } finally {
      afterProcess();
    }
  }

  protected void afterProcess() {
  }

  protected void onSuccess() {
  }

  protected String[] getTopPolicy() {
    return requiredToPolicy;
  }

  protected void onCustomException(AbstractResultMessageException e) {
    throw e;
  }

  protected void onException(RuntimeException e) {
    throw e;
  }

  protected void onResourceExistException() {
    throw new ResourceExisted();
  }

  protected void onException(Throwable e) {
    throw new RuntimeException(e);
  }

  public String getBizKey0() {
    return bizKey0;
  }

  public void enableMultiTenantCtrl() {
    this.multiTenantCtrl = true;
    setMultiTenantCtrl(true);
  }

  public void closeMultiTenantCtrl() {
    this.multiTenantCtrl = false;
    setMultiTenantCtrl(false);
  }

  protected void checkPolicy() {
    // PrincipalContext.checkOpMultiTenantPermission(); // Trigger by permission annotation @see PrincipalPermissionService
  }
}
