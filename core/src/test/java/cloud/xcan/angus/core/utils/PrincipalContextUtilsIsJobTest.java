package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.spec.principal.Principal;
import org.junit.jupiter.api.Test;

class PrincipalContextUtilsIsJobTest {

  @Test
  void isJob_trueWhenNoApiTypeAndDefaultClientIdOnPrincipal() {
    Principal p = new Principal();
    p.setApiType(null);
    p.setClientId(Principal.DEFAULT_CLIENT_ID);
    assertTrue(PrincipalContextUtils.isJob(p));
  }

  @Test
  void isJob_falseWhenClientIdSet() {
    Principal p = new Principal();
    p.setApiType(null);
    p.setClientId("job-runner");
    assertFalse(PrincipalContextUtils.isJob(p));
  }

  @Test
  void isJob_falseWhenApiTypeSet() {
    Principal p = new Principal();
    p.setApiType(ApiType.API);
    p.setClientId(Principal.DEFAULT_CLIENT_ID);
    assertFalse(PrincipalContextUtils.isJob(p));
  }
}
