
package cloud.xcan.sdf.core.biz;


import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.decideMultiTenantCtrlByApiType;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.hasOriginalOptTenantId;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isOpClient;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isPrivateEdition;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.setMultiTenantCtrl;

import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BizTemplate<T> {

  protected String bizKey0;
  protected transient boolean defaultMultiTenantCtrl;
  protected transient boolean multiTenantAutoCtrlWhenOpClient;
  private final String[] requiredToPolicy;

  protected BizTemplate() {
    this(true);
  }

  protected BizTemplate(String bizKey0) {
    this(bizKey0, true, false);
  }

  protected BizTemplate(boolean defaultMultiTenantCtrl) {
    this("", defaultMultiTenantCtrl, false);
  }

  protected BizTemplate(boolean defaultMultiTenantCtrl, String... requiredToPolicy) {
    this("", defaultMultiTenantCtrl, false, requiredToPolicy);
  }

  protected BizTemplate(boolean defaultMultiTenantCtrl, boolean multiTenantAutoCtrlWhenOpClient) {
    this("", defaultMultiTenantCtrl, multiTenantAutoCtrlWhenOpClient);
  }

  protected BizTemplate(boolean defaultMultiTenantCtrl, boolean multiTenantAutoCtrlWhenOpClient,
      String... requiredToPolicy) {
    this("", defaultMultiTenantCtrl, multiTenantAutoCtrlWhenOpClient, requiredToPolicy);
  }

  protected BizTemplate(String bizKey0, boolean defaultMultiTenantCtrl,
      boolean multiTenantAutoCtrlWhenOpClient, String... requiredToPolicy) {
    this.bizKey0 = bizKey0;
    this.defaultMultiTenantCtrl = defaultMultiTenantCtrl;
    this.multiTenantAutoCtrlWhenOpClient = multiTenantAutoCtrlWhenOpClient;
    this.requiredToPolicy = requiredToPolicy;
    Principal principal = PrincipalContext.get();
    if (ObjectUtils.isNotEmpty(requiredToPolicy)) {
      // Used by TenantInterceptor
      principal.setRequiredToPolicy(requiredToPolicy);
    }
    if (!defaultMultiTenantCtrl) {
      principal.setMultiTenantCtrl(false);
    } else if (!decideMultiTenantCtrlByApiType(principal)) {
      principal.setMultiTenantCtrl(false);
    } else {
      boolean autoClosed = false;
      if (this.multiTenantAutoCtrlWhenOpClient) {
        // Attempt to close automatically
        autoClosed = closeMultiTenantCtrlIfNonOptTenantId();
      }
      // Open when closing fails
      if (!autoClosed) {
        principal.setMultiTenantCtrl(true);
      }
    }
  }

  protected abstract void checkParams();

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
      if (isUserAction() && isPrivateEdition()) {
        //TODO checkAccess(0.01);
      }
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

  public boolean isDefaultMultiTenantCtrl() {
    return this.defaultMultiTenantCtrl;
  }

  public boolean isMultiTenantAutoCtrlWhenOpClient() {
    return this.multiTenantAutoCtrlWhenOpClient;
  }

  public void enableMultiTenantCtrl() {
    this.defaultMultiTenantCtrl = true;
    setMultiTenantCtrl(true);
  }

  public void closeMultiTenantCtrl() {
    this.defaultMultiTenantCtrl = false;
    setMultiTenantCtrl(false);
  }

  public boolean closeMultiTenantCtrlIfNonOptTenantId() {
    if (isOpClient() && !hasOriginalOptTenantId()) {
      closeMultiTenantCtrl();
      return true;
    }
    return false;
  }

  protected void checkPolicy() {
    // PrincipalContext.checkOpMultiTenantPermission(); // Trigger by permission annotation @see PrincipalPermissionService
  }
}
