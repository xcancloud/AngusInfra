package cloud.xcan.sdf.spec.obfuscated;

import cloud.xcan.sdf.api.obf.Obj0;

public class ValueObfuscatedTest {

  public static void main(String[] args) {
    // SpecConstant
    System.out.println(Obj0.var0("locale"));
    System.out.println(Obj0.var0("timeZone"));
    System.out.println(Obj0.var0("Asia/Shanghai"));
    System.out.println(Obj0.var0("localeCookie"));

    // AESValue
    System.out.println(Obj0.var0("AES"));
    System.out.println(Obj0.var0("XCAN"));

    // BizConstant
    System.out.println(Obj0.var0("xcan_tp"));
    System.out.println(Obj0.var0("xcan_op"));
    System.out.println(Obj0.var0("xcan_2p"));
    System.out.println(Obj0.var0("xcan_3rd"));
    System.out.println(Obj0.var0("password"));
    System.out.println(Obj0.var0("client_secret"));
  }
}
