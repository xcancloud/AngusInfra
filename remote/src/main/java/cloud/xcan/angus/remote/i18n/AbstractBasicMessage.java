package cloud.xcan.angus.remote.i18n;

import java.util.Locale;

/**
 * A basic message implementation.
 */
public abstract class AbstractBasicMessage implements Message {

  @Override
  public final String toString() {
    return toString(Locale.getDefault());
  }
}
