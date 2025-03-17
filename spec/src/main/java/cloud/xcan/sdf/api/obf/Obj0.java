package cloud.xcan.sdf.api.obf;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Obfuscated
 */
public class Obj0 {

  private static final String T
      = new String(new char[]{'\u0055', '\u0054', '\u0046', '\u0038'}); // => "UTF8"

  public static String var0(final String v9) {
    if (-1 != v9.indexOf(0)) {
      throw new IllegalArgumentException(new Str0(new long[]{
          0x241005931110FC70L, 0xDCD925A88EAD9F37L, 0x19ADA1C861E2A85DL,
          0x9A5948E700FCAD8AL, 0x2E11C83A72441DE2L
      }).toString()); // => "Null characters are not allowed!";
    }

    final byte[] v3;
    try {
      v3 = v9.getBytes(T);
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex); // UTF8 is always supported
    }

    final Random v1 = new Random(); // randomly seeded
    final long v6 = v1.nextLong(); // seed strength is effectively 48 bits
    v1.setSeed(v6);

    final StringBuffer v0 = new StringBuffer(new Str0(
        new long[]{0xCE527FE35028B296L, 0x11745B0625524D48L, 0xBB055ADC674E7C53L,
            0xBCF79328EF92D106L}).toString()); /* => "new Str0(new long[] {" */

    var0(v0, v6);

    final int length = v3.length;
    for (int i = 0; i < length; i += 8) {
      final long v10 = v1.nextLong();
      final long v11 = var0(v3, i) ^ v10;

      v0.append(", ");
      var0(v0, v11);
    }

    v0.append(new Str0(new long[]{
        0xC6C9D2C6C10415A6L, 0x4E95F934E283C522L, 0xA08583BEE3DC26F0L, 0x8154D85BDD79AEC9L
    }).toString()); // => "}).toString() /* => \"";

    v0.append(
        v9.replaceAll("\\\\", new Str0(new long[]{
            0xA29CBADD304D66B3L, 0x3BC0533312572E8L}).toString() /* => "\\\\\\\\" */)
            .replaceAll("\"", new Str0(new long[]{
                0x634FF2A8FA61783DL, 0x7BF71E47CF3FE42EL}).toString() /* => "\\\\\"" */));

    v0.append(new Str0(new long[]{
        0x5A7EC5031E4CEB56L, 0x961C2C6BB6146855L
    }).toString()); // => "\" */"

    return v0.toString();
  }

  private static long var0(final byte[] v1, int v2) {
    final int end = Math.min(v1.length, v2 + 8);
    long l = 0;
    for (int i = end; --i >= v2; ) {
      l <<= 8;
      l |= v1[i] & 0xFF;
    }
    return l;
  }

  private static void var0(final StringBuffer v1, final long v2) {
    v1.append('0'); // obfuscation futile - too short
    v1.append('x'); // dito
    v1.append(Long.toHexString(v2).toUpperCase());
    v1.append('L'); // dito
  }
}
