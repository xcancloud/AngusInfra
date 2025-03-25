package cloud.xcan.angus.validator;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X10;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.angus.validator.impl.EditorContentLengthValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Check the valid length of quill editor content.
 */
@Documented
@Constraint(validatedBy = EditorContentLengthValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface EditorContentLength {

  long max() default DEFAULT_DESC_LENGTH_X10;

  EditorType value() default EditorType.QUILL;

  String message() default "{xcan.validator.constraints.EditorContentLength.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
