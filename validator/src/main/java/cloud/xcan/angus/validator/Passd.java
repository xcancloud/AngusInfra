package cloud.xcan.angus.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cloud.xcan.angus.validator.impl.PassdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PassdValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Passd {

  boolean allowNull() default false;

  boolean allowUpperCase() default true;

  boolean allowLowerCase() default true;

  boolean allowSpecialChar() default true;

  boolean allowDigits() default true;

  int allowMinTypeNum() default 2;

  double allowMaxRepeatRate() default 0.5;

  int minSize() default 6;

  int maxSize() default 50;

  String message() default "{xcan.validator.constraints.Passd.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
