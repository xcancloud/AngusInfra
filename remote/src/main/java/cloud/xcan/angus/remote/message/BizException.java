package cloud.xcan.angus.remote.message;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.ERROR;
import static cloud.xcan.angus.remote.message.BizException.M.BIZ_ERROR;
import static cloud.xcan.angus.remote.message.BizException.M.BIZ_ERROR_KEY;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>
 * Business exception class for handling application-specific business logic errors.
 * This exception is designed to be handled by the global exception handler and
 * typically returns HTTP status 200 with error details in the response body.
 * </p>
 * 
 * <p>
 * Key features:
 * - Structured error information with code, message, and additional context
 * - Support for parameterized error messages with internationalization
 * - Flexible exception levels (ERROR, WARNING, INFO, DEBUG)
 * - Comprehensive factory methods for various construction scenarios
 * - Integration with global exception handling framework
 * - Support for exception chaining and root cause analysis
 * </p>
 * 
 * <p>
 * Usage patterns:
 * <pre>
 * // Simple business error
 * throw BizException.of("User not found");
 * 
 * // Error with custom key for internationalization
 * throw BizException.of("user.not.found", "USER_NOT_FOUND");
 * 
 * // Error with parameters for message formatting
 * throw BizException.of("user.invalid.age", new Object[]{userId, age});
 * 
 * // Error with underlying cause
 * throw BizException.of("Database connection failed", cause);
 * 
 * // Warning-level business exception
 * throw BizException.of("Data validation warning", ExceptionLevel.WARNING);
 * </pre>
 * </p>
 * 
 * <p>
 * Exception Levels:
 * - ERROR: Critical business errors that require immediate attention
 * - WARNING: Business rule violations that may need user action
 * - INFO: Informational messages about business state changes
 * - DEBUG: Detailed diagnostic information for troubleshooting
 * </p>
 * 
 * <p>
 * Thread Safety: This class is immutable and thread-safe once constructed.
 * </p>
 * 
 * @see AbstractResultMessageException
 * @see ExceptionLevel
 * @see EventType
 */
@Getter
@ToString
public class BizException extends AbstractResultMessageException {

  /**
   * The business error code associated with this exception.
   * This code is used for programmatic error handling and client-side logic.
   */
  private final String code;

  /**
   * The human-readable error message.
   * This message may contain placeholders for parameterization.
   */
  private final String msg;

  /**
   * Arguments for parameterized error messages.
   * These arguments are used to format the message template with actual values.
   */
  private final Object[] args;

  /**
   * <p>
   * Default constructor creating a generic business error.
   * Uses default error message and ERROR level.
   * </p>
   */
  public BizException() {
    this(BIZ_ERROR, BIZ_ERROR_KEY, null, ERROR);
  }

  /**
   * <p>
   * Constructor for creating a business exception with full context.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for internationalization and categorization
   * @param args the arguments for message parameterization
   * @param level the severity level of the exception
   */
  public BizException(String message, String eKey, Object[] args, ExceptionLevel level) {
    this(message, eKey, args, level, null);
  }

  /**
   * <p>
   * Full constructor for creating a business exception with all available context.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for internationalization and categorization
   * @param args the arguments for message parameterization
   * @param level the severity level of the exception
   * @param cause the underlying cause of this exception
   */
  public BizException(String message, String eKey, Object[] args, ExceptionLevel level,
      Throwable cause) {
    super(message, EventType.BUSINESS, level, eKey, cause);
    this.code = BUSINESS_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  /* ==================== Basic Factory Methods ==================== */

  /**
   * <p>
   * Creates a business exception with a simple error message.
   * Uses default error key and ERROR level.
   * </p>
   *
   * @param message the error message
   * @return a new BizException instance
   */
  public static BizException of(String message) {
    return new BizException(message, BIZ_ERROR_KEY, null, ERROR);
  }

  /**
   * <p>
   * Creates a business exception with a message and underlying cause.
   * </p>
   *
   * @param message the error message
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, Throwable cause) {
    return new BizException(message, BIZ_ERROR_KEY, null, ERROR, cause);
  }

  /**
   * <p>
   * Creates a business exception with a message and custom error key.
   * The error key can be used for internationalization and error categorization.
   * </p>
   *
   * @param message the error message
   * @param eKey the error key for categorization
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey) {
    return new BizException(message, eKey, null, ERROR);
  }

  /**
   * <p>
   * Creates a business exception with message, error key, and underlying cause.
   * </p>
   *
   * @param message the error message
   * @param eKey the error key for categorization
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, Throwable cause) {
    return new BizException(message, eKey, null, ERROR, cause);
  }

  /* ==================== Parameterized Message Factory Methods ==================== */

  /**
   * <p>
   * Creates a business exception with a parameterized message.
   * The arguments are used to format the message template.
   * </p>
   *
   * @param message the error message template
   * @param args the arguments for message formatting
   * @return a new BizException instance
   */
  public static BizException of(String message, Object[] args) {
    return new BizException(message, BIZ_ERROR_KEY, args, ERROR);
  }

  /**
   * <p>
   * Creates a business exception with parameterized message and underlying cause.
   * </p>
   *
   * @param message the error message template
   * @param args the arguments for message formatting
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, Object[] args, Throwable cause) {
    return new BizException(message, BIZ_ERROR_KEY, args, ERROR, cause);
  }

  /**
   * <p>
   * Creates a business exception with parameterized message and custom error key.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for categorization
   * @param args the arguments for message formatting
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, Object[] args) {
    return new BizException(message, eKey, args, ERROR);
  }

  /**
   * <p>
   * Creates a business exception with full parameterization and underlying cause.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for categorization
   * @param args the arguments for message formatting
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, Object[] args, Throwable cause) {
    return new BizException(message, eKey, args, ERROR, cause);
  }

  /* ==================== Exception Level Factory Methods ==================== */

  /**
   * <p>
   * Creates a business exception with a specific severity level.
   * Uses default error message and key.
   * </p>
   *
   * @param level the severity level of the exception
   * @return a new BizException instance
   */
  public static BizException of(ExceptionLevel level) {
    return new BizException(BIZ_ERROR, BIZ_ERROR_KEY, null, level);
  }

  /**
   * <p>
   * Creates a business exception with severity level and underlying cause.
   * </p>
   *
   * @param level the severity level of the exception
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(ExceptionLevel level, Throwable cause) {
    return new BizException(BIZ_ERROR, BIZ_ERROR_KEY, null, level, cause);
  }

  /**
   * <p>
   * Creates a business exception with custom message and severity level.
   * </p>
   *
   * @param message the error message
   * @param level the severity level of the exception
   * @return a new BizException instance
   */
  public static BizException of(String message, ExceptionLevel level) {
    return new BizException(message, BIZ_ERROR_KEY, null, level);
  }

  /**
   * <p>
   * Creates a business exception with message, severity level, and underlying cause.
   * </p>
   *
   * @param message the error message
   * @param level the severity level of the exception
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, ExceptionLevel level, Throwable cause) {
    return new BizException(message, BIZ_ERROR_KEY, null, level, cause);
  }

  /**
   * <p>
   * Creates a business exception with message, error key, and severity level.
   * </p>
   *
   * @param message the error message
   * @param eKey the error key for categorization
   * @param level the severity level of the exception
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, ExceptionLevel level) {
    return new BizException(message, eKey, null, level);
  }

  /**
   * <p>
   * Creates a business exception with message, error key, severity level, and cause.
   * </p>
   *
   * @param message the error message
   * @param eKey the error key for categorization
   * @param level the severity level of the exception
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, ExceptionLevel level,
      Throwable cause) {
    return new BizException(message, eKey, null, level, cause);
  }

  /* ==================== Full-Featured Factory Methods ==================== */

  /**
   * <p>
   * Creates a business exception with parameterized message and severity level.
   * </p>
   *
   * @param message the error message template
   * @param args the arguments for message formatting
   * @param level the severity level of the exception
   * @return a new BizException instance
   */
  public static BizException of(String message, Object[] args, ExceptionLevel level) {
    return new BizException(message, BIZ_ERROR_KEY, args, level);
  }

  /**
   * <p>
   * Creates a business exception with parameterized message, severity level, and cause.
   * </p>
   *
   * @param message the error message template
   * @param args the arguments for message formatting
   * @param level the severity level of the exception
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, Object[] args, ExceptionLevel level,
      Throwable cause) {
    return new BizException(message, BIZ_ERROR_KEY, args, level, cause);
  }

  /**
   * <p>
   * Creates a business exception with all available parameters except cause.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for categorization
   * @param args the arguments for message formatting
   * @param level the severity level of the exception
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, Object[] args,
      ExceptionLevel level) {
    return new BizException(message, eKey, args, level);
  }

  /**
   * <p>
   * Creates a business exception with all available parameters.
   * This is the most comprehensive factory method providing full control
   * over all exception attributes.
   * </p>
   *
   * @param message the error message template
   * @param eKey the error key for categorization
   * @param args the arguments for message formatting
   * @param level the severity level of the exception
   * @param cause the underlying cause
   * @return a new BizException instance
   */
  public static BizException of(String message, String eKey, Object[] args,
      ExceptionLevel level, Throwable cause) {
    return new BizException(message, eKey, args, level, cause);
  }

  /**
   * <p>
   * Constants for default business exception messages and keys.
   * These constants provide standardized error identifiers for common scenarios.
   * </p>
   */
  public interface M {

    /**
     * Default business error message key for internationalization.
     */
    String BIZ_ERROR = "xcm.biz.error";

    /**
     * Default business error key for categorization and logging.
     */
    String BIZ_ERROR_KEY = "business_error";
  }
}
