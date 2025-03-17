package cloud.xcan.sdf.core.utils;

import cloud.xcan.sdf.spec.utils.NetworkUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NetworkUtilsTest {

  @Test
  public void testGetHostName(){
    String hostName = NetworkUtils.getHostName();
    Assert.assertNotNull(hostName);
  }

  @Test
  public void testGetIpV4(){
    String ip = NetworkUtils.getValidIpv4();
    System.out.println(ip);
    Assert.assertTrue(ip != null && !ip.equals("127.0.0.1"));
    List<String> ips = NetworkUtils.getValidIpv4s();
    System.out.println(ips);
    Assert.assertTrue(ips.stream().anyMatch(x->x.startsWith("192.168.")));
  }
}
