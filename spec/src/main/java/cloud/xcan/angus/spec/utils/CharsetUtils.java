package cloud.xcan.angus.spec.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Charset resolution helpers.
 */
public final class CharsetUtils {

  private CharsetUtils() {
  }

  /**
   * Resolves a charset by name, or {@code null} if {@code name} is null or unknown.
   */
  public static Charset lookup(final String name) {
    if (name == null) {
      return null;
    }
    try {
      return Charset.forName(name);
    } catch (final UnsupportedCharsetException ex) {
      return null;
    }
  }

  /**
   * Like {@link #lookup(String)} but throws when the name is unknown; returns {@code null} only when
   * {@code name} is {@code null}.
   */
  public static Charset get(final String name) throws UnsupportedEncodingException {
    if (name == null) {
      return null;
    }
    try {
      return Charset.forName(name);
    } catch (final UnsupportedCharsetException ex) {
      throw new UnsupportedEncodingException(name);
    }
  }
}
