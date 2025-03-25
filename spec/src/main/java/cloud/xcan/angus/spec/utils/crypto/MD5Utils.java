package cloud.xcan.angus.spec.utils.crypto;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public final class MD5Utils {

  public static String encrypt(File file) {
    InputStream in = null;
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      in = new FileInputStream(file);
      byte[] buffer = new byte[1024];
      int readLen;
      while ((readLen = in.read(buffer)) != -1) {
        digest.update(buffer, 0, readLen);
      }
      return toHex(digest.digest());
    } catch (Exception e) {
      return null;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
        // Nothing
      }
    }
  }

  public static String encrypt(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(text.getBytes(UTF_8));
      return toHex(digest.digest());
    } catch (Exception e) {
      return null;
    }
  }

  public static String encrypt(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(bytes);
      return toHex(digest.digest());
    } catch (Exception e) {
      return null;
    }
  }

  private static String toHex(byte[] bytes) {
    StringBuilder buffer = new StringBuilder(bytes.length * 2);
    for (byte aByte : bytes) {
      buffer.append(Character.forDigit((aByte & 240) >> 4, 16));
      buffer.append(Character.forDigit(aByte & 15, 16));
    }
    return buffer.toString();
  }
}
