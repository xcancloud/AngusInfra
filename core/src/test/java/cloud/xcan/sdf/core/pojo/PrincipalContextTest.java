package cloud.xcan.sdf.core.pojo;

import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.setMultiTenantCtrl;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.setOptTenantId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

public class PrincipalContextTest {

  @Test
  public void principalThreadSaleTest() throws InterruptedException {
    System.out.println("Main Thread：" + Thread.currentThread().getName());
    CountDownLatch count = new CountDownLatch(1);
    Principal principal1 = new Principal();
    Principal principal2 = new Principal();
    PrincipalContext.set(principal1);
    Thread thread1 = new Thread(() -> {
      PrincipalContext.set(principal2);
      System.out.println("Sub Thread：" + Thread.currentThread().getName());
      assertEquals(PrincipalContext.get(), principal2);
      count.countDown();
    });
    thread1.start();
    count.await();
    assertEquals(PrincipalContext.get(), principal1);
  }

  @Test
  public void principalValueSetterTest() {
    Principal pi = new Principal().setRequestId("1")
        .setMultiTenantCtrl(false)
        .setOptTenantId(-1L);
    PrincipalContext.set(pi);
    assertEquals(PrincipalContext.get(), pi);
    assertEquals(PrincipalContext.get().getRequestId(), pi.getRequestId());
    assertEquals(PrincipalContext.get().isMultiTenantCtrl(), pi.isMultiTenantCtrl());
    assertEquals(PrincipalContext.get().getOptTenantId(), pi.getOptTenantId());
    setMultiTenantCtrl(true);
    assertTrue(isMultiTenantCtrl());
    setOptTenantId(-2L);
    assertEquals(-2L, (long) getOptTenantId());
    assertEquals(-2L, (long) PrincipalContext.get().getOptTenantId());
  }

}
