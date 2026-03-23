package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.spec.utils.NetworkUtils;
import java.util.List;
import org.junit.jupiter.api.Test;

class NetworkUtilsTest {

  @Test
  void testGetHostName() {
    String hostName = NetworkUtils.getHostName();
    assertNotNull(hostName);
    assertFalse(hostName.isBlank());
  }

  @Test
  void testGetIpV4() {
    String ip = NetworkUtils.getValidIpv4();
    assertNotNull(ip);
    assertTrue(NetworkUtils.isIpAddress(ip), () -> "Expected IPv4 text, got: " + ip);

    List<String> ips = NetworkUtils.getValidIpv4s();
    if (ips == null || ips.isEmpty()) {
      assertEquals("127.0.0.1", ip,
          "When no non-loopback IPv4 exists, getValidIpv4() must fall back to 127.0.0.1");
      return;
    }
    assertTrue(ips.stream().allMatch(NetworkUtils::isIpAddress));
  }
}
