package cloud.xcan.angus.spec.version;

import cloud.xcan.angus.spec.version.VersionParser.CharType;
import java.util.Arrays;

/**
 * Thrown when attempting to consume a character of unexpected types.
 * <p>
 * This exception is a wrapper exception extending {@code ParseException}.
 */
public class UnexpectedCharacterException extends ParseException {

  /**
   * The unexpected character.
   */
  private final Character unexpected;

  /**
   * The position of the unexpected character.
   */
  private final int position;

  /**
   * The array of expected character types.
   */
  private final CharType[] expected;

  /**
   * Constructs a {@code UnexpectedCharacterException} instance with the wrapped
   * {@code UnexpectedElementException} exception.
   *
   * @param cause the wrapped exception
   */
  UnexpectedCharacterException(UnexpectedElementException cause) {
    position = cause.getPosition();
    unexpected = (Character) cause.getUnexpectedElement();
    expected = (CharType[]) cause.getExpectedElementTypes();
  }

  /**
   * Constructs a {@code UnexpectedCharacterException} instance with the unexpected character, its
   * position and the expected types.
   *
   * @param unexpected the unexpected character
   * @param position   the position of the unexpected character
   * @param expected   an array of the expected character types
   */
  UnexpectedCharacterException(
      Character unexpected,
      int position,
      CharType... expected
  ) {
    this.unexpected = unexpected;
    this.position = position;
    this.expected = expected;
  }

  /**
   * Gets the unexpected character.
   *
   * @return the unexpected character
   */
  public Character getUnexpectedCharacter() {
    return unexpected;
  }

  /**
   * Gets the position of the unexpected character.
   *
   * @return the position of the unexpected character
   */
  public int getPosition() {
    return position;
  }

  /**
   * Gets the expected character types.
   *
   * @return an array of expected character types
   */
  public CharType[] getExpectedCharTypes() {
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
        "Unexpected character '%s(%s)' at position '%d'",
        CharType.forCharacter(unexpected),
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
