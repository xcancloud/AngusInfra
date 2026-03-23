package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 异步线程应携带 Principal 快照；执行完毕后线程上下文应清理，避免线程池串租户。
 */
class TenantAwareTaskDecoratorTest {

  private final TenantAwareTaskDecorator decorator = new TenantAwareTaskDecorator();

  @AfterEach
  void tearDown() {
    PrincipalContext.remove();
  }

  @Test
  void decorate_propagatesPrincipalSnapshotAndRestoresThreadLocal() throws Exception {
    Principal p = new Principal()
        .setTenantId(501L)
        .setOptTenantId(502L)
        .setMultiTenantCtrl(true);
    PrincipalContext.set(p);

    AtomicReference<Long> seen = new AtomicReference<>();
    Runnable inner = () -> seen.set(PrincipalContext.get().getTenantId());
    Runnable wrapped = decorator.decorate(inner);

    PrincipalContext.remove();
    assertNull(PrincipalContext.threadLocal.get());

    Thread t = new Thread(wrapped);
    t.start();
    t.join();

    assertEquals(501L, seen.get());
    assertNull(PrincipalContext.threadLocal.get());
  }

  @Test
  void decorate_withoutBoundPrincipal_runsTaskUnwrapped() {
    PrincipalContext.remove();
    AtomicReference<Boolean> ran = new AtomicReference<>(false);
    Runnable wrapped = decorator.decorate(() -> ran.set(true));
    wrapped.run();
    assertEquals(true, ran.get());
  }
}
