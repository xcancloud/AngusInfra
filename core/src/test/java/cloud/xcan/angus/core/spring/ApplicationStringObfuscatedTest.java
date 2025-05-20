package cloud.xcan.angus.core.spring;

import cloud.xcan.angus.api.obf.Obj0;
import cloud.xcan.angus.api.obf.Str0;
import org.springframework.boot.system.ApplicationHome;

public class ApplicationStringObfuscatedTest {

  static String SALT = "435E9A3AB63ED118";

  static String SYSTEM_TOKEN_SALT = "XCanSysToken";
  static String USER_TOKEN_SALT = "XCanUserToken";

  public static void main(String[] args) {
    ApplicationHome home = new ApplicationHome(ApplicationStringObfuscatedTest.class);
    System.out.println(home.getSource().getAbsolutePath());
    System.out.println(home.getDir().getAbsolutePath());

    System.out.println(Obj0.var0("/openapi2p"));

    System.out.println(Obj0.var0("SimpleEvent"));
    System.out.println(Obj0.var0("xcanDisruptorQueueSimpleEvent"));

    System.out.println(Obj0.var0(SALT));
    System.out.println(
        new Str0(new long[]{0x7A8583F2887CDD91L, 0x35CE04A478ED551AL, 0xDA6B6B48ABF61AA9L})
            .toString() /* => "435E9A3AB63ED118" */);

    System.out.println(Obj0.var0(SYSTEM_TOKEN_SALT));
    // Encryption
    System.out.println(
        new Str0(new long[]{0x231236753DF64C33L, 0x3E304342F297835DL, 0x84FB5E43B36A0C85L})
            .toString() /* => "XCanSysToken" */);
    // Decryption
    System.out.println(
        new Str0(new long[]{0x14FCDD296C72B045L, 0xAF8B12E2ADE53624L, 0xCB8EBDC6C911124EL})
            .toString() /* => "XCanSysToken" */);

    // Encryption
    System.out.println(Obj0.var0(USER_TOKEN_SALT));
    System.out.println(
        new Str0(new long[]{0x4E212140E4A50C75L, 0xB611A4BC975C4BFBL, 0x4831FED56D6B4BF8L})
            .toString() /* => "XCanUserToken" */);
    // Decryption
    System.out.println(
        new Str0(new long[]{0x863731077291C1CBL, 0xE57910A474C48E17L, 0xE35535CC75D03B07L})
            .toString() /* => "XCanUserToken" */);
  }
}
