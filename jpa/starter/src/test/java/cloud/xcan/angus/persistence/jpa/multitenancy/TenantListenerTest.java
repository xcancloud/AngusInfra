package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAware;
import cloud.xcan.angus.core.jpa.multitenancy.TenantListener;
import cloud.xcan.angus.spec.experimental.BizConstant;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import org.junit.jupiter.api.Test;

/**
 * 实体生命周期：在开启多租户且 API 判定需要隔离时，{@link TenantListener} 应写入当前 opt/tenant id。
 */
class TenantListenerTest extends PrincipalTestSupport {

  private final TenantListener listener = new TenantListener();

  @Test
  void beforeAnyUpdate_setsTenantWhenMultiTenantCtrlAndApiApplies() {
    bindPrincipal(apiTenantPrincipal(100L, null));
    TenantAware entity = mock(TenantAware.class);

    listener.beforeAnyUpdate(entity);

    verify(entity).setTenantId(100L);
  }

  @Test
  void beforeAnyUpdate_usesOptTenantIdForOperationClient() {
    bindPrincipal(new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setClientId(BizConstant.XCAN_OPERATION_PLATFORM_CODE)
        .setTenantId(200L)
        .setOptTenantId(300L));
    TenantAware entity = mock(TenantAware.class);

    listener.beforeAnyUpdate(entity);

    verify(entity).setTenantId(300L);
  }

  @Test
  void beforeAnyUpdate_skipsWhenMultiTenantCtrlDisabled() {
    bindPrincipal(multiTenantOff(50L));
    TenantAware entity = mock(TenantAware.class);

    listener.beforeAnyUpdate(entity);

    verifyNoInteractions(entity);
  }

  @Test
  void beforeAnyUpdate_skipsWhenDecideMultiTenantByApiTypeFalse() {
    bindPrincipal(viewNoOptTenant(60L));
    TenantAware entity = mock(TenantAware.class);

    listener.beforeAnyUpdate(entity);

    verifyNoInteractions(entity);
  }

  @Test
  void principalContext_notLeakedAfterTestBaseClears() {
    bindPrincipal(apiTenantPrincipal(1L));
    assertEquals(1L, PrincipalContext.get().getTenantId());
    PrincipalContext.remove();
    assertNull(PrincipalContext.threadLocal.get());
  }
}
