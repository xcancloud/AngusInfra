package cloud.xcan.angus.spec.utils.mc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * MD5 + display formatting for machine codes.
 */
public final class Util {

  private static final HexFormat HEX = HexFormat.of();

  private Util() {
  }

  /**
   * MD5 digest of {@code input} as lowercase hex (32 chars). {@code null} input yields {@code null}.
   */
  public static String md5(String input) {
    if (input == null) {
      return null;
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HEX.formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm not available", e);
    }
  }

  /**
   * Formats a 32-char hex MD5 into {@code XXXX-XXXX-XXXX-XXXX} using bytes 8–23 (inclusive) of the
   * hex string (i.e. substring indices {@code [8, 24)}).
   *
   * @throws IllegalArgumentException if {@code md5Hex} is null or shorter than 24 characters
   */
  public static String format(String md5Hex) {
    if (md5Hex == null || md5Hex.length() < 24) {
      throw new IllegalArgumentException("md5Hex must be non-null and at least 24 characters");
    }
    String str = md5Hex.substring(8, 24);
    StringBuilder sb = new StringBuilder(19);
    for (int i = 0; i < str.length(); i++) {
      if (i % 4 == 0 && i != 0) {
        sb.append('-');
      }
      sb.append(str.charAt(i));
    }
    return sb.toString().toUpperCase(Locale.ROOT);
  }
}
