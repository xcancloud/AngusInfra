package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class IpAddressUtilTest {

  @Test
  void isValidIp_ipv4() {
    assertTrue(IpAddressUtil.isValidIp("192.168.1.1"));
    assertTrue(IpAddressUtil.isValidIp("0.0.0.0"));
    assertTrue(IpAddressUtil.isValidIp("255.255.255.255"));
  }

  @Test
  void isValidIp_ipv4_invalid() {
    assertFalse(IpAddressUtil.isValidIp("256.1.1.1"));
    assertFalse(IpAddressUtil.isValidIp("192.168.1"));
    assertFalse(IpAddressUtil.isValidIp(""));
    assertFalse(IpAddressUtil.isValidIp(null));
  }

  @Test
  void isValidIp_ipv6_loopback_expanded() {
    assertTrue(IpAddressUtil.isValidIp("0:0:0:0:0:0:0:1"));
  }

  @Test
  void isInternalIp() {
    assertTrue(IpAddressUtil.isInternalIp("127.0.0.1"));
    assertTrue(IpAddressUtil.isInternalIp("localhost"));
    assertTrue(IpAddressUtil.isInternalIp("192.168.0.1"));
    assertTrue(IpAddressUtil.isInternalIp("10.0.0.1"));
    assertTrue(IpAddressUtil.isInternalIp("172.16.0.1"));
    assertTrue(IpAddressUtil.isInternalIp("172.31.255.1"));
    assertFalse(IpAddressUtil.isInternalIp("8.8.8.8"));
    assertFalse(IpAddressUtil.isInternalIp("172.32.0.1"));
    assertFalse(IpAddressUtil.isInternalIp(null));
  }

  @Test
  void getClientIpAddress_prefersFirstValidInXForwardedFor() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.2");
    when(req.getRemoteAddr()).thenReturn("10.0.0.1");
    assertEquals("203.0.113.1", IpAddressUtil.getClientIpAddress(req));
  }

  @Test
  void getClientIpAddress_fallsBackToRemoteAddr() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader("X-Forwarded-For")).thenReturn(null);
    when(req.getRemoteAddr()).thenReturn("198.51.100.10");
    assertEquals("198.51.100.10", IpAddressUtil.getClientIpAddress(req));
  }
}
