package cloud.xcan.angus.spec.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class CharsetUtils {

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
