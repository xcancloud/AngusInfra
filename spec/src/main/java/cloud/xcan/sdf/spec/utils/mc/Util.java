package cloud.xcan.sdf.spec.utils.mc;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;

/**
 * General utility methods
 */
@Slf4j
public final class Util {

  private Util() {
  }

  public static String md5(String input) {
    if (null == input) {
      return null;
    }
    String md5 = null;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(input.getBytes(), 0, input.length());
      md5 = new BigInteger(1, messageDigest.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return md5;
  }

  public static String format(String md5) {
    String str = md5.substring(8, 24);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      if (i % 4 == 0 && i !=0) {
        sb.append("-");
      }
      sb.append(str.charAt(i));
    }
    return sb.toString().toUpperCase();
  }
}
