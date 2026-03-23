package cloud.xcan.angus.spec.utils.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * SHA-256 helpers (e.g. token fingerprinting).
 */
public final class SHAUtils {

  private static final HexFormat HEX = HexFormat.of();

  private SHAUtils() {
  }

  /**
   * SHA-256 hash of {@code token} as lowercase hex.
   */
  public static String hashToken(String token) {
    Objects.requireNonNull(token, "token");
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(UTF_8));
      return HEX.formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException("SHA-256 not available", e);
    }
  }
}
