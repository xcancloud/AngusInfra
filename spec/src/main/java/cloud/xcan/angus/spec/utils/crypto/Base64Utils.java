package cloud.xcan.angus.spec.utils.crypto;


import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

public class Base64Utils {

  /**
   * Decode BASE64 string to binary data
   */
  public static byte[] decode(String base64) {
    return Base64.getDecoder().decode(base64.getBytes(UTF_8));
  }

  /**
   * Binary data is encoded as a BASE64 string
   */
  public static String encode(byte[] bytes) {
    return new String(Base64.getEncoder().encode(bytes), UTF_8);
  }

}
