package cloud.xcan.angus.spec.experimental;

import java.nio.charset.Charset;

/**
 * Constant definitions for the standard {@link Charset Charsets}. These charsets are guaranteed to
 * be available on every implementation of the Java platform.
 *
 * @see <a href="Charset#standard">Standard Charsets</a>
 */
public final class StandardCharsets {

  private StandardCharsets() {
    throw new AssertionError(
        "No cloud.xcan.angus.spec.experimental.StandardCharsets instances for you!");
  }

  /**
   * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
   */
  public static final Charset US_ASCII = java.nio.charset.StandardCharsets.US_ASCII;
  /**
   * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
   */
  public static final Charset ISO_8859_1 = java.nio.charset.StandardCharsets.ISO_8859_1;
  /**
   * Eight-bit UCS Transformation Format
   */
  public static final Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;
  /**
   * Sixteen-bit UCS Transformation Format, big-endian byte order
   */
  public static final Charset UTF_16BE = java.nio.charset.StandardCharsets.UTF_16BE;
  /**
   * Sixteen-bit UCS Transformation Format, little-endian byte order
   */
  public static final Charset UTF_16LE = java.nio.charset.StandardCharsets.UTF_16LE;
  /**
   * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
   */
  public static final Charset UTF_16 = java.nio.charset.StandardCharsets.UTF_16;
}
