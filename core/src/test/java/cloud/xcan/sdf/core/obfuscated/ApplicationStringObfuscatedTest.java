package cloud.xcan.angus.core.obfuscated;

import cloud.xcan.angus.api.obf.Obj0;
import cloud.xcan.angus.api.obf.Str0;
import org.springframework.boot.system.ApplicationHome;

public class ApplicationStringObfuscatedTest {

  static String CLOUD_LCS_NO = "BBQQ-G8HZ-NK2M-QKNA-XQ7U";
  static String SALT = ".435E9A3AB63ED118";

  static String INSTALL_LCS_PASSD = "5UL1KO7E1CEGPXXR";

  static String SYSTEM_TOKEN_SALT = "ST.hrvZDSLkwUjsox38ZeeL8ZjkxXXqgvZ7";
  static String USER_TOKEN_SALT = "UT.FqwUmPVdItfUiLT13yI2KnOw0NprPLWv";

  public static void main(String[] args) {
    ApplicationHome home = new ApplicationHome(ApplicationStringObfuscatedTest.class);
    System.out.println(home.getSource().getAbsolutePath());
    System.out.println(home.getDir().getAbsolutePath());

    System.out.println(Obj0.var0("Read license exception[WRITE_ERROR]"));
    System.out.println(Obj0.var0("Read license exception[SIGNATURE_ERROR]"));
    System.out.println(Obj0.var0("Read license exception[LCS_MODIFIED]"));
    System.out.println(Obj0.var0("Read license exception[LCS_ERROR]"));
    System.out.println(Obj0.var0("Read license exception[LIB_PATH_ERROR]"));
    System.out.println(Obj0.var0("Read license exception[CHECK_EXCEPTION]"));
    System.out.println(Obj0.var0("Main license exception[EXPIRATION]"));
    System.out.println(Obj0.var0("Main license exception[ERROR]"));
    System.out.println(Obj0.var0("Main license exception[TO_LCS_ERROR]"));

    System.out.println(Obj0.var0(CLOUD_LCS_NO));

    System.out.println(Obj0.var0("/openapi2p"));

    System.out.println(Obj0.var0("SimpleEvent"));
    System.out.println(Obj0.var0("xcanDisruptorQueueSimpleEvent"));

    // Install or replace lcs
    System.out.println(Obj0.var0(INSTALL_LCS_PASSD));

    System.out.println(Obj0.var0(SALT));
    System.out.println(
        new Str0(new long[]{0x7A8583F2887CDD91L, 0x35CE04A478ED551AL, 0xDA6B6B48ABF61AA9L})
            .toString() /* => "435E9A3AB63ED118" */);

    // SystemToken
    System.out.println(Obj0.var0(SYSTEM_TOKEN_SALT));
    // Encryption
    System.out.println(
        new Str0(new long[]{0x2E3179AED36C394EL, 0x7BBCD7A2FDBD0DB6L, 0x7E457ADBE5A4A864L,
            0xAD38EEC763A58504L}).toString() /* => "Vb5E9AOpAc3EDc8UHN" */);
    // Decryption
    System.out.println(
        new Str0(new long[]{0x9D957430D3C25453L, 0x3131DF7E7B7C7DD3L, 0xFC13465E273A71CEL})
            .toString() /* => "XCanSystemToken" */);

    // UserToken
    // Encryption
    System.out.println(Obj0.var0(USER_TOKEN_SALT));
    System.out.println(
        new Str0(new long[]{0x44221B366B4C9D1BL, 0x7EB3E83EA07EFD45L, 0x3CF9CFD71B6A6DCBL,
            0xF78174480B8C15F7L}).toString() /* => "Va5a9A1pNN3E0B8JGL" */);
    // Decryption
    System.out.println(
        new Str0(new long[]{0x863731077291C1CBL, 0xE57910A474C48E17L, 0xE35535CC75D03B07L})
            .toString() /* => "XCanUserToken" */);
  }
}
