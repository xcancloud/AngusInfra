package cloud.xcan.angus.remote.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Looks up the resources for this package in a Resource Bundle. Provided for comfort.
 */
class MessageResources {

  private static final ResourceBundle RESOURCES = ResourceBundle.getBundle("i18n/messages");

  /**
   * Looks up a string resource identified by {@code key} in {@code resources}.
   */
  public static String getString(String key) {
    return RESOURCES.getString(key);
  }

  /**
   * Looks up a string resource identified by {@code key} in {@code resources} and formats it as a
   * message using {@code MessageFormat.format} with the given {@code arguments}.
   */
  public static String getString(String key, Object[] arguments) {
    return MessageFormat.format(getString(key), arguments);
  }

  /**
   * Looks up a string resource identified by {@code key} in {@code resources} and formats it as a
   * message using {@code MessageFormat.format} with the given singular {@code argument}.
   */
  public static String getString(String key, Object argument) {
    return MessageFormat.format(getString(key), argument);
  }

  /**
   * You cannot instantiate this class.
   */
  protected MessageResources() {
  }
}
