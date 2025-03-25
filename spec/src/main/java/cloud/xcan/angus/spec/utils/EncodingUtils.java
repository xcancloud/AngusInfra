package cloud.xcan.angus.spec.utils;

import static cloud.xcan.angus.spec.experimental.StandardCharsets.US_ASCII;

import cloud.xcan.angus.spec.experimental.Assert;
import java.io.UnsupportedEncodingException;

/**
 * The home for utility methods that handle various encoding tasks.
 */
public final class EncodingUtils {

  /**
   * This class should not be instantiated.
   */
  private EncodingUtils() {
  }

  /**
   * Converts the byte array of HTTP content characters to a string. If the specified charset is not
   * supported, default system encoding is used.
   *
   * @param data    the byte array to be encoded
   * @param offset  the index of the first byte to encode
   * @param length  the number of bytes to encode
   * @param charset the desired character encoding
   * @return The result of the conversion.
   */
  public static String getString(final byte[] data, final int offset,
      final int length, final String charset) {
    Assert.assertNotNull(data, "Input");
    Assert.assertNotEmpty(charset, "Charset");
    try {
      return new String(data, offset, length, charset);
    } catch (final UnsupportedEncodingException e) {
      return new String(data, offset, length);
    }
  }


  /**
   * Converts the byte array of HTTP content characters to a string. If the specified charset is not
   * supported, default system encoding is used.
   *
   * @param data    the byte array to be encoded
   * @param charset the desired character encoding
   * @return The result of the conversion.
   */
  public static String getString(final byte[] data, final String charset) {
    Assert.assertNotNull(data, "Input");
    return getString(data, 0, data.length, charset);
  }

  /**
   * Converts the specified string to a byte array.  If the charset is not supported the default
   * system charset is used.
   *
   * @param data    the string to be encoded
   * @param charset the desired character encoding
   * @return The resulting byte array.
   */
  public static byte[] getBytes(final String data, final String charset) {
    Assert.assertNotNull(data, "Input");
    Assert.assertNotEmpty(charset, "Charset");
    try {
      return data.getBytes(charset);
    } catch (final UnsupportedEncodingException e) {
      return data.getBytes();
    }
  }

  /**
   * Converts the specified string to byte array of ASCII characters.
   *
   * @param data the string to be encoded
   * @return The string as a byte array.
   */
  public static byte[] getAsciiBytes(final String data) {
    Assert.assertNotNull(data, "Input");
    return data.getBytes(US_ASCII);
  }

  /**
   * Converts the byte array of ASCII characters to a string. This method is to be used when
   * decoding content of HTTP elements (such as response headers)
   *
   * @param data   the byte array to be encoded
   * @param offset the index of the first byte to encode
   * @param length the number of bytes to encode
   * @return The string representation of the byte array
   */
  public static String getAsciiString(final byte[] data, final int offset, final int length) {
    Assert.assertNotNull(data, "Input");
    return new String(data, offset, length, US_ASCII);
  }

  /**
   * Converts the byte array of ASCII characters to a string. This method is to be used when
   * decoding content of HTTP elements (such as response headers)
   *
   * @param data the byte array to be encoded
   * @return The string representation of the byte array
   */
  public static String getAsciiString(final byte[] data) {
    Assert.assertNotNull(data, "Input");
    return getAsciiString(data, 0, data.length);
  }

}
