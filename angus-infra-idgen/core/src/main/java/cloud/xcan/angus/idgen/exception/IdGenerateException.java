package cloud.xcan.angus.idgen.exception;

/**
 * IdGenerateException
 *
 * @author XiaoLong Liu
 */
public class IdGenerateException extends RuntimeException {

  /**
   * Serial Version UID
   */
  private static final long serialVersionUID = -1L;

  /**
   * Default constructor
   */
  public IdGenerateException() {
    super();
  }

  /**
   * Constructor with msg & cause
   */
  public IdGenerateException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with msg
   */
  public IdGenerateException(String message) {
    super(message);
  }

  /**
   * Constructor with msg format
   */
  public IdGenerateException(String msgFormat, Object... args) {
    super(String.format(msgFormat, args));
  }

  /**
   * Constructor with cause
   */
  public IdGenerateException(Throwable cause) {
    super(cause);
  }

}
