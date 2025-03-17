package cloud.xcan.sdf.spec;


public class EventObject<T> implements java.io.Serializable {

  /**
   * The object on which the Event initially occurred.
   */
  public T source;

  /**
   * Constructs a empty Event.
   */
  public EventObject() {
  }

  /**
   * Constructs a prototypical Event.
   *
   * @param source The object on which the Event initially occurred.
   * @throws IllegalArgumentException if source is null.
   */
  public EventObject(T source) {
    if (source == null) {
      throw new IllegalArgumentException("null source");
    }
    this.source = source;
  }

  /**
   * The object on which the Event initially occurred.
   *
   * @return The object on which the Event initially occurred.
   */
  public T getSource() {
    return source;
  }

  /**
   * @param source The object on which the Event initially occurred.
   * @throws IllegalArgumentException if source is null.
   */
  public void setSource(T source) {
    if (source == null) {
      throw new IllegalArgumentException("null source");
    }
    this.source = source;
  }

  /**
   * Returns a String representation of this EventObject.
   *
   * @return A a String representation of this EventObject.
   */
  @Override
  public String toString() {
    return getClass().getName() + "[source=" + source + "]";
  }
}
