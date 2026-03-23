package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.enums.ReceiveObjectType;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EventUtilsTest {

  @AfterEach
  void tearDown() {
    PrincipalContext.remove();
  }

  @Test
  void assembleExceptionEvent_mapsPrincipalAndNullCause() {
    Principal p = new Principal();
    p.setClientId("cli-1");
    p.setServiceCode("svc");
    p.setRequestId("req-9");
    PrincipalContext.set(p);

    EventContent ec = EventUtils.assembleExceptionEvent("EX", "E01", "msg",
        ExceptionLevel.WARNING, "ek", null);

    assertEquals("cli-1", ec.getClientId());
    assertEquals("svc", ec.getServiceCode());
    assertEquals("req-9", ec.getRequestId());
    assertEquals("ek", ec.getEKey());
    assertEquals(ExceptionLevel.WARNING, ec.getLevel());
    assertNull(ec.getCause());
  }

  @Test
  void assembleExceptionEvent_causeAsString() {
    PrincipalContext.set(new Principal());
    EventContent ec = EventUtils.assembleExceptionEvent("EX", "E01", "m",
        ExceptionLevel.ERROR, "k", new IllegalArgumentException("bad"));
    assertEquals("java.lang.IllegalArgumentException: bad", ec.getCause());
  }

  @Test
  void assembleNoticeEventByDoor_usesProvidedPrincipal() {
    Principal p = new Principal();
    p.setClientId("door-cli");
    p.setTenantId(100L);

    EventContent ec = EventUtils.assembleNoticeEventByDoor("appX", "NT", p, "C1", "hello",
        "USER", "u1", "User One",
        List.of(NoticeType.EMAIL), ReceiveObjectType.USER, List.of(1L),
        List.of("pol"), null);

    assertEquals("door-cli", ec.getClientId());
    assertEquals("appX", ec.getAppCode());
    assertEquals(Long.valueOf(100L), ec.getTenantId());
    assertEquals("u1", ec.getTargetId());
  }
}
