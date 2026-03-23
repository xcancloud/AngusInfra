package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.api.enums.DeviceType;
import cloud.xcan.angus.api.pojo.DeviceInfo;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

class DeviceInfoExtractorUtilsTest {

  @Test
  void parseDeviceType_mobile() {
    assertEquals(DeviceType.MOBILE,
        DeviceInfoExtractorUtils.parseDeviceType("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)"));
  }

  @Test
  void parseDeviceType_desktop() {
    assertEquals(DeviceType.DESKTOP,
        DeviceInfoExtractorUtils.parseDeviceType(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91"));
  }

  @Test
  void parsePlatform_andBrowser() {
    assertEquals("Windows",
        DeviceInfoExtractorUtils.parsePlatform("Mozilla/5.0 (Windows NT 10.0)"));
    assertEquals("Chrome",
        DeviceInfoExtractorUtils.parseBrowser("Mozilla/5.0 Chrome/91.0 Safari/537.36"));
  }

  @Test
  void extractDeviceInfo_readsHeaders() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(Header.USER_AGENT)).thenReturn("ua-test");
    when(req.getHeader(Header.AUTH_APP_VERSION)).thenReturn("1.2.3");
    when(req.getHeader(Header.AUTH_DEVICE_ID)).thenReturn("dev-xyz");

    DeviceInfo info = DeviceInfoExtractorUtils.extractDeviceInfo(req);
    assertEquals("ua-test", info.getUserAgent());
    assertEquals("1.2.3", info.getAppVersion());
    assertEquals("dev-xyz", info.getDeviceId());
  }

  @Test
  void generateTemporaryDeviceId_stableForSameSessionAndIp() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(req.getSession()).thenReturn(session);
    when(session.getId()).thenReturn("sid");
    when(req.getHeader("X-Forwarded-For")).thenReturn(null);
    when(req.getRemoteAddr()).thenReturn("127.0.0.1");

    String a = DeviceInfoExtractorUtils.generateTemporaryDeviceId(req);
    String b = DeviceInfoExtractorUtils.generateTemporaryDeviceId(req);
    assertNotNull(a);
    assertEquals(16, a.length());
    assertEquals(a, b);
  }
}
