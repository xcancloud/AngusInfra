package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.core.spring.filter.MutableHttpServletRequest;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class ServletUtilsTest {

  @Test
  void getRequestId_generatesWhenMissing() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(Header.REQUEST_ID)).thenReturn(null);
    String id = ServletUtils.getRequestId(req);
    assertTrue(StringUtils.isNotBlank(id));
  }

  @Test
  void getRequestId_usesHeaderWhenPresent() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(Header.REQUEST_ID)).thenReturn("rid-123");
    assertEquals("rid-123", ServletUtils.getRequestId(req));
  }

  @Test
  void getAndSetRequestId_putsGeneratedIdWhenBlank() {
    HttpServletRequest base = mock(HttpServletRequest.class);
    when(base.getHeader(Header.REQUEST_ID)).thenReturn(null);
    MutableHttpServletRequest req = new MutableHttpServletRequest(base);
    String id = ServletUtils.getAndSetRequestId(req);
    assertFalse(id.isBlank());
    assertEquals(id, req.getHeader(Header.REQUEST_ID));
  }

  @Test
  void getAuthServiceCode_defaultWhenMissing() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(AuthKey.AUTH_SERVICE_CODE)).thenReturn(null);
    assertEquals(AuthKey.DEFAULT_AUTH_SERVICE_CODE, ServletUtils.getAuthServiceCode(req));
  }

  @Test
  void getAuthServiceCode_uppercases() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(AuthKey.AUTH_SERVICE_CODE)).thenReturn("mySvc");
    assertEquals("MYSVC", ServletUtils.getAuthServiceCode(req));
  }
}
