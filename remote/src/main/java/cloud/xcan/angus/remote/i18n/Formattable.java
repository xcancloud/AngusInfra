package cloud.xcan.angus.remote.i18n;

/**
 * A formattable object.
 */
public interface Formattable {

  /**
   * Formats the message keyed by this object with the given arguments.
   *
   * @param args the formatting arguments. Implementations may add constraints to these.
   * @return the formatted message.
   */
  Message format(Object... args);
}
