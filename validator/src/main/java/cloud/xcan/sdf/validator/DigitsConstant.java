package cloud.xcan.sdf.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.sdf.validator.impl.DigitsConstantValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = DigitsConstantValidator.class)
public @interface DigitsConstant {

  public abstract int[] array();

  public abstract String message() default "{xcan.validator.constraints.DigitsConstant.message}";

  public abstract Class<?>[] groups() default {};

  public abstract Class<? extends Payload>[] payload() default {};

  @Target({METHOD, FIELD, PARAMETER, ANNOTATION_TYPE})
  @Retention(RUNTIME)
  @Documented
  public @interface List {

    DigitsConstant[] value();
  }
}
