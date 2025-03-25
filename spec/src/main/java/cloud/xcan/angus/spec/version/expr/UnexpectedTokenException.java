package cloud.xcan.angus.spec.version.expr;

import cloud.xcan.angus.spec.version.ParseException;
import cloud.xcan.angus.spec.version.UnexpectedElementException;
import cloud.xcan.angus.spec.version.expr.Lexer.Token;
import java.util.Arrays;

/**
 * Thrown when a token of unexpected types is encountered during the parsing.
 *
 * @author Zafar Khaja <zafarkhaja@gmail.com>
 * @since 0.7.0
 */
public class UnexpectedTokenException extends ParseException {

  /**
   * The unexpected token.
   */
  private final Token unexpected;

  /**
   * The array of the expected token types.
   */
  private final Token.Type[] expected;

  /**
   * Constructs a {@code UnexpectedTokenException} instance with the wrapped
   * {@code UnexpectedElementException} exception.
   *
   * @param cause the wrapped exception
   */
  UnexpectedTokenException(UnexpectedElementException cause) {
    unexpected = (Token) cause.getUnexpectedElement();
    expected = (Token.Type[]) cause.getExpectedElementTypes();
  }

  /**
   * Constructs a {@code UnexpectedTokenException} instance with the unexpected token and the
   * expected types.
   *
   * @param token    the unexpected token
   * @param expected an array of the expected token types
   */
  UnexpectedTokenException(Token token, Token.Type... expected) {
    unexpected = token;
    this.expected = expected;
  }

  /**
   * Gets the unexpected token.
   *
   * @return the unexpected token
   */
  public Token getUnexpectedToken() {
    return unexpected;
  }

  /**
   * Gets the expected token types.
   *
   * @return an array of expected token types
   */
  public Token.Type[] getExpectedTokenTypes() {
    return expected;
  }

  /**
   * Returns the string representation of this exception containing the information about the
   * unexpected token and, if available, about the expected types.
   *
   * @return the string representation of this exception
   */
  @Override
  public String toString() {
    String message = String.format(
        "Unexpected token '%s'",
        unexpected
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
