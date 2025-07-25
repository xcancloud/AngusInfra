package cloud.xcan.angus.validator;

import cloud.xcan.angus.validator.impl.IDValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Custom validation annotation for ID fields that ensures the value is a valid
 * identifier within specified constraints. This annotation validates that the
 * ID value is positive, non-zero, and within the maximum allowed range.
 * </p>
 * 
 * <p>
 * Key features:
 * - Validates positive ID values (greater than 0)
 * - Configurable maximum value constraint
 * - Integration with Jakarta Bean Validation framework
 * - Supports method parameters, fields, and return values
 * - Custom error message support with internationalization
 * </p>
 * 
 * <p>
 * Usage examples:
 * <pre>
 * // Basic usage with default maximum value
 * &#64;ID
 * private Long userId;
 * 
 * // Custom maximum value constraint
 * &#64;ID(max = 999999)
 * private Long customId;
 * 
 * // Method parameter validation
 * public User findUser(&#64;ID Long userId) {
 *     // Implementation
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Validation rules:
 * - Value must be greater than 0
 * - Value must be less than or equal to the specified maximum
 * - Null values are considered valid (use @NotNull for null checks)
 * </p>
 * 
 * @see IDValidator
 * @see jakarta.validation.constraints.Positive
 * @see jakarta.validation.constraints.Max
 */
@Documented
@Constraint(validatedBy = {IDValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ID {

  /**
   * <p>
   * Specifies the maximum allowed value for the ID.
   * The validated value must be less than or equal to this maximum.
   * </p>
   * 
   * <p>
   * Default value is {@link IDValidator#MAX_ID_VALUE}, which provides
   * a reasonable upper bound for most ID use cases.
   * </p>
   *
   * @return the maximum allowed ID value
   */
  long max() default IDValidator.MAX_ID_VALUE;

  /**
   * <p>
   * The error message to be interpolated when validation fails.
   * Supports internationalization through message keys.
   * </p>
   * 
   * <p>
   * The default message key can be overridden in ValidationMessages.properties
   * to provide localized error messages.
   * </p>
   *
   * @return the error message template
   */
  String message() default "{xcan.validator.constraints.ID.message}";

  /**
   * <p>
   * Allows the specification of validation groups, to which this constraint belongs.
   * This enables conditional validation based on the validation context.
   * </p>
   *
   * @return the groups the constraint belongs to
   */
  Class<?>[] groups() default {};

  /**
   * <p>
   * Can be used by clients of the Bean Validation API to assign custom payload
   * objects to a constraint. This attribute is not used by the API itself.
   * </p>
   *
   * @return the payload associated to the constraint
   */
  Class<? extends Payload>[] payload() default {};
}
