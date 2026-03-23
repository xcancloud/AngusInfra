package cloud.xcan.angus.spec.utils.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** MD5 digests (legacy / non-cryptographic checksums only). */
public final class MD5Utils {

  private static final HexFormat HEX = HexFormat.of();

  private MD5Utils() {
  }

  public static String encrypt(File file) {
    Objects.requireNonNull(file, "file");
    try (InputStream in = Files.newInputStream(file.toPath())) {
      MessageDigest digest = getDigest();
      byte[] buffer = new byte[8192];
      int readLen;
      while ((readLen = in.read(buffer)) != -1) {
        digest.update(buffer, 0, readLen);
      }
      return HEX.formatHex(digest.digest());
    } catch (IOException e) {
      throw new CryptoException("MD5 file digest failed: " + file, e);
    }
  }

  public static String encrypt(String text) {
    Objects.requireNonNull(text, "text");
    MessageDigest digest = getDigest();
    digest.update(text.getBytes(UTF_8));
    return HEX.formatHex(digest.digest());
  }

  public static String encrypt(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    return HEX.formatHex(getDigest().digest(bytes));
  }

  private static MessageDigest getDigest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoException("MD5 algorithm not available", e);
    }
  }
}
