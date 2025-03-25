package cloud.xcan.angus.spec.version;

import cloud.xcan.angus.spec.version.TypeStream.ElementType;
import java.util.Arrays;

/**
 * Thrown when attempting to consume a stream element of unexpected types.
 *
 * @see TypeStream#consume(ElementType...)
 */
public class UnexpectedElementException extends RuntimeException {

  /**
   * The unexpected element in the stream.
   */
  private final Object unexpected;

  /**
   * The position of the unexpected element in the stream.
   */
  private final int position;

  /**
   * The array of the expected element types.
   */
  private final ElementType<?>[] expected;

  /**
   * Constructs a {@code UnexpectedElementException} instance with the unexpected element and the
   * expected types.
   *
   * @param element  the unexpected element in the stream
   * @param position the position of the unexpected element
   * @param expected an array of the expected element types
   */
  UnexpectedElementException(
      Object element,
      int position,
      ElementType<?>... expected
  ) {
    unexpected = element;
    this.position = position;
    this.expected = expected;
  }

  /**
   * Gets the unexpected element.
   *
   * @return the unexpected element
   */
  public Object getUnexpectedElement() {
    return unexpected;
  }

  /**
   * Gets the position of the unexpected element.
   *
   * @return the position of the unexpected element
   */
  public int getPosition() {
    return position;
  }

  /**
   * Gets the expected element types.
   *
   * @return an array of expected element types
   */
  public ElementType<?>[] getExpectedElementTypes() {
    return expected;
  }

  /**
   * Returns the string representation of this exception containing the information about the
   * unexpected element and, if available, about the expected types.
   *
   * @return the string representation of this exception
   */
  @Override
  public String toString() {
    String message = String.format(
        "Unexpected element '%s' at position '%d'",
        unexpected,
        position
    );
    if (expected.length > 0) {
      message += String.format(
          ", expecting '%s'",
          Arrays.toString(expected)
      );
    }
    return message;
  }
}
