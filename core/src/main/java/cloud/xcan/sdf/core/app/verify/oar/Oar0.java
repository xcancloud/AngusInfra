package cloud.xcan.sdf.core.app.verify.oar;

import java.util.Random;

/**
 * Obfuscated
 */
public class Oar0 {

  private static final String var2 =
      new String(new char[]{'\u0055', '\u0054', '\u0046', '\u0038'});

  public static String var0(final String s) {
    if (-1 != s.indexOf(0)) {
      throw new IllegalArgumentException(new Oar1(new long[]{
          0x241005931110FC70L, 0xDCD925A88EAD9F37L, 0x19ADA1C861E2A85DL,
          0x9A5948E700FCAD8AL, 0x2E11C83A72441DE2L
      }).toString()); // => "Null characters are not allowed!";
    }

    final byte[] b0;
    try {
      b0 = s.getBytes(var2);
    } catch (Exception ex) {
      throw new AssertionError(ex);
    }

    final Random b1 = new Random();
    final long b2 = b1.nextLong();
    b1.setSeed(b2);

    final StringBuffer b3 = new StringBuffer(new Oar1(
        new long[]{0x319ED461DB75FC53L, 0x8DCB85A4776B2F2EL, 0x98BB2230CD38967CL,
            0xC91E4A5F09F2CA78L}).toString()); /* => "new Oar0(new long[] {" */

    var0(b3, b2);

    final int b4 = b0.length;
    for (int i = 0; i < b4; i += 8) {
      final long b7 = b1.nextLong();
      final long b5 = var0(b0, i) ^ b7;
      b3.append(", ");
      var0(b3, b5);
    }

    b3.append(new Oar1(new long[]{
        0xC6C9D2C6C10415A6L, 0x4E95F934E283C522L, 0xA08583BEE3DC26F0L, 0x8154D85BDD79AEC9L
    }).toString());

    b3.append(
        s.replaceAll("\\\\", new Oar1(new long[]{
            0xA29CBADD304D66B3L, 0x3BC0533312572E8L}).toString())
            .replaceAll("\"", new Oar1(new long[]{
                0x634FF2A8FA61783DL, 0x7BF71E47CF3FE42EL}).toString()));

    b3.append(new Oar1(new long[]{
        0x5A7EC5031E4CEB56L, 0x961C2C6BB6146855L
    }).toString());

    return b3.toString();
  }

  private static void var0(final StringBuffer b1, final long b2) {
    b1.append('0');
    b1.append('x');
    b1.append(Long.toHexString(b2).toUpperCase());
    b1.append('L');
  }

  private static long var0(final byte[] b1, int b2) {
    final int end = Math.min(b1.length, b2 + 8);
    long l = 0;
    for (int i = end; --i >= b2; ) {
      l <<= 8;
      l |= b1[i] & 0xFF;
    }
    return l;
  }

  public static void main(String[] args) {
    System.out.println(Oar0.var0("mln"));
    System.out.println(Oar0.var0(".435E9A3AB63ED118"));
  }
}
