package cloud.xcan.sdf.spec.version.expr;

import cloud.xcan.sdf.spec.version.ParseException;

/**
 * Thrown during the lexical analysis when an illegal character is encountered.
 */
public class LexerException extends ParseException {

  /**
   * The string being analyzed starting from an illegal character.
   */
  private final String expr;

  /**
   * Constructs a {@code LexerException} instance with a string starting from an illegal character.
   *
   * @param expr the string starting from an illegal character
   */
  LexerException(String expr) {
    this.expr = expr;
  }

  /**
   * Returns the string representation of this exception.
   *
   * @return the string representation of this exception
   */
  @Override
  public String toString() {
    return "Illegal character near '" + expr + "'";
  }
}
