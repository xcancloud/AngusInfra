package cloud.xcan.sdf.spec.version;

/**
 * Thrown to indicate an error during the parsing.
 */
public class ParseException extends RuntimeException {

  /**
   * Constructs a {@code ParseException} instance with no error message.
   */
  public ParseException() {
    super();
  }

  /**
   * Constructs a {@code ParseException} instance with an error message.
   *
   * @param message the error message
   */
  public ParseException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code ParseException} instance with an error message and the cause exception.
   *
   * @param message the error message
   * @param cause   an exception that caused this exception
   */
  public ParseException(String message, UnexpectedCharacterException cause) {
    super(message);
    initCause(cause);
  }

  /**
   * Returns the string representation of this exception.
   *
   * @return the string representation of this exception
   */
  @Override
  public String toString() {
    Throwable cause = getCause();
    String msg = getMessage();
    if (msg != null) {
      msg += ((cause != null) ? " (" + cause.toString() + ")" : "");
      return msg;
    }
    return ((cause != null) ? cause.toString() : "");
  }
}
