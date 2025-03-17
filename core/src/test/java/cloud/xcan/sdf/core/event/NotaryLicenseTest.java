package cloud.xcan.sdf.core.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import cloud.xcan.sdf.api.obf.Str0;
import cloud.xcan.sdf.core.app.verify.oar.Oar0;
import cloud.xcan.sdf.core.app.verify.ver.Guard;
import org.junit.Test;

public class NotaryLicenseTest {

  String licenseKeypass = new Str0(new long[] {0x26660259AC059E29L, 0x874CC109D814FC60L, 0x40647729D23B2DCDL, 0x4632A9FCD988251DL}).toString() /* => "BBQQ-G8HZ-NK2M-QKNA-XQ7U" */ + ".435E9A3AB63ED118";
  String licensePath = "/workdata/keystore/license.lic";

  @Test
  public void test() throws Exception {
    Guard guard = new Guard(licenseKeypass, licensePath);
    assertNotNull(guard.var101());
    assertNotNull(guard.var102());
    assertNotNull(guard.var103());
    assertNotNull(guard.var104());
    assertNotNull(guard.var105());
    assertNotNull(guard.var106());
    assertNotNull(guard.var107());
    assertNotNull(guard.var108());
    assertNotNull(guard.var109());
    assertNotNull(guard.var110());
    assertNotNull(guard.var111());
    assertNotNull(guard.var112());
    assertNotNull(guard.var113());
    assertNotNull(guard.var114());
    assertNotNull(guard.var115());
    assertNotNull(guard.var116());
    assertNotNull(guard.var117());
    assertNotNull(guard.var118());
    assertNotNull(guard.var119());
    assertNotNull(guard.var120());
    assertNotNull(guard.var121());
    assertNotNull(guard.var122());
    assertNotNull(guard.var123());
    assertNotNull(guard.var124());
    assertNotNull(guard.var125());
  }

  @Test
  public void testObf() {
    //    System.out.println(Oar0.var0("consumerAmount"));
    //    System.out.println(Oar0.var0("consumerType"));
    //    System.out.println(Oar0.var0("extra"));
    //    System.out.println(Oar0.var0("holder"));
    //    System.out.println(Oar0.var0("info"));
    //    System.out.println(Oar0.var0("issuedDate"));
    //    System.out.println(Oar0.var0("issuer"));
    //    System.out.println(Oar0.var0("notAfter"));
    //    System.out.println(Oar0.var0("notBefore"));
    //    System.out.println(Oar0.var0("subject"));
    //    System.out.println(Oar0.var0("provider"));
    //    System.out.println(Oar0.var0("productType"));
    //    System.out.println(Oar0.var0("versionType"));
    //    System.out.println(Oar0.var0("version"));
    //    System.out.println(Oar0.var0("orderNo"));
    //    System.out.println(Oar0.var0("ipAddress"));
    //    System.out.println(Oar0.var0("macAddress"));
    //    System.out.println(Oar0.var0("cpuSerial"));
    //    System.out.println(Oar0.var0("mainBoardSerial"));
    //    System.out.println(Oar0.var0("testConcurrency"));
    //    System.out.println(Oar0.var0("testNodeNumber"));
    //    System.out.println(Oar0.var0("holderId"));
    //    System.out.println(Oar0.var0("clientId"));
    //    System.out.println(Oar0.var0("clientSecret"));
    //    System.out.println(Oar0.var0("productCode"));
    System.out.println(Oar0.var0("cam"));
    System.out.println(Oar0.var0("cty"));
    System.out.println(Oar0.var0("ext"));
    System.out.println(Oar0.var0("hol"));
    System.out.println(Oar0.var0("inf"));
    System.out.println(Oar0.var0("ida"));
    System.out.println(Oar0.var0("iss"));
    System.out.println(Oar0.var0("naf"));
    System.out.println(Oar0.var0("nbe"));
    System.out.println(Oar0.var0("sub"));
    System.out.println(Oar0.var0("pro"));
    System.out.println(Oar0.var0("pty"));
    System.out.println(Oar0.var0("vty"));
    System.out.println(Oar0.var0("ver"));
    System.out.println(Oar0.var0("ono"));
    System.out.println(Oar0.var0("iad"));
    System.out.println(Oar0.var0("mad"));
    System.out.println(Oar0.var0("cse"));
    System.out.println(Oar0.var0("mbo"));
    System.out.println(Oar0.var0("tco"));
    System.out.println(Oar0.var0("tno"));
    System.out.println(Oar0.var0("hid"));
    System.out.println(Oar0.var0("cid"));
    System.out.println(Oar0.var0("csec"));
    System.out.println(Oar0.var0("pco"));
  }

}
