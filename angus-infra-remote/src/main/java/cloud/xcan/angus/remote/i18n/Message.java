package cloud.xcan.angus.remote.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * An internationalized message.
 */
public interface Message extends Serializable {

  /**
   * Returns a message for the default locale.
   */
  @Override
  String toString();

  /**
   * Returns a message for the given locale.
   */
  String toString(Locale locale);
}
