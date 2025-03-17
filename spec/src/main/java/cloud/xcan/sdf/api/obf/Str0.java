package cloud.xcan.sdf.api.obf;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Obfuscate revert
 */
final public class Str0 {

  private static final String UTF8
      = new String(new char[]{'\u0055', '\u0054', '\u0046', '\u0038'}); // => "UTF8"

  private final long[] o;

  public Str0(final long[] var0) {
    this.o = (long[]) var0.clone();
    this.o[0] = var0[0];
  }

  private static void toBytes(long l, byte[] bytes, int off) {
    final int end = Math.min(bytes.length, off + 8);
    for (int i = off; i < end; i++) {
      bytes[i] = (byte) l;
      l >>= 8;
    }
  }

  /**
   * Returns the original string.
   */
  @Override
  public String toString() {
    final int length = o.length;

    // The original UTF8 encoded string was probably not a multiple
    // of eight bytes long and is thus actually shorter than this array.
    final byte[] encoded = new byte[8 * (length - 1)];

    // Obtain the seed and initialize a new PRNG with it.
    final long seed = o[0];
    final Random prng = new Random(seed);

    // De-obfuscate.
    for (int i = 1; i < length; i++) {
      final long key = prng.nextLong();
      toBytes(o[i] ^ key, encoded, 8 * (i - 1));
    }

    // Decode the UTF-8 encoded byte array into a string.
    // This will create null characters at the end of the decoded string
    // in case the original UTF8 encoded string was not a multiple of
    // eight bytes long.
    final String decoded;
    try {
      decoded = new String(encoded, UTF8);
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex); // UTF-8 is always supported
    }

    // Cut off trailing null characters in case the original UTF8 encoded
    // string was not a multiple of eight bytes long.
    final int i = decoded.indexOf(0);
    return -1 == i ? decoded : decoded.substring(0, i);
  }

}
