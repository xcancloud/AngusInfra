package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 临时关闭多租户控制时，即使任务抛错也应恢复 {@link Principal} 状态。
 */
class TenantAwareProcessorTest {

  private final TenantAwareProcessor processor = new TenantAwareProcessor();

  @AfterEach
  void tearDown() {
    PrincipalContext.remove();
  }

  @Test
  void run_restoresMultiTenantCtrlAfterExceptionWhenNoOptOverride() {
    Principal p = new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(10L)
        .setOptTenantId(null);
    PrincipalContext.set(p);

    assertThrows(RuntimeException.class,
        () -> processor.run(() -> {
          throw new RuntimeException("boom");
        }, null));

    assertTrue(PrincipalContext.get().isMultiTenantCtrl());
  }

  @Test
  void call_restoresMultiTenantCtrlAfterExceptionWhenNoOptOverride() {
    Principal p = new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(ApiType.API)
        .setTenantId(1L)
        .setOptTenantId(null);
    PrincipalContext.set(p);

    assertThrows(IllegalStateException.class,
        () -> processor.call(() -> {
          throw new IllegalStateException("fail");
        }, null));

    assertTrue(PrincipalContext.get().isMultiTenantCtrl());
    assertEquals(1L, PrincipalContext.get().getTenantId());
  }
}
