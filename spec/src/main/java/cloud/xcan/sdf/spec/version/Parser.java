package cloud.xcan.sdf.spec.version;

/**
 * A parser interface.
 *
 * @param <T> the type of parser's output
 */
public interface Parser<T> {

  /**
   * Parses the input string.
   *
   * @param input the string to parse
   * @return the Abstract Syntax Tree
   */
  T parse(String input);
}
