package cloud.xcan.angus.spec.utils.crypto;

import java.util.Base64;
import java.util.Objects;

/** Base64 helpers (UTF-8 for text round-trips). */
public final class Base64Utils {

  private Base64Utils() {
  }

  /** Decodes a Base64 string (URL-safe alphabet not supported; use standard Base64 only). */
  public static byte[] decode(String base64) {
    Objects.requireNonNull(base64, "base64");
    return Base64.getDecoder().decode(base64);
  }

  /** Encodes binary data as a standard Base64 ASCII string. */
  public static String encode(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    return Base64.getEncoder().encodeToString(bytes);
  }
}
