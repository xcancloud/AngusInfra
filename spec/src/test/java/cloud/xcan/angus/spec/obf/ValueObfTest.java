package cloud.xcan.angus.spec.obf;

import cloud.xcan.angus.api.obf.Obj0;

public class ValueObfTest {

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

    System.out.println(Obj0.var0("keyId"));
    System.out.println(Obj0.var0("keySecret"));

    System.out.println(Obj0.var0("init_application_cache"));
    System.out.println(Obj0.var0("check_application_cache"));

    System.out.println(Obj0.var0("XCAN_TP_SIGNIN"));
    System.out.println(Obj0.var0("XCAN_OP_SIGNIN"));
    System.out.println(Obj0.var0("XCAN_USER_TOKEN"));
    System.out.println(Obj0.var0("XCAN_SYS_TOKEN"));
    System.out.println(Obj0.var0("XCAN_2P_SIGNIN"));

  }
}
