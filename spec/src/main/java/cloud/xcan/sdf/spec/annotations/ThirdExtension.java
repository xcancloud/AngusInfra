package cloud.xcan.sdf.spec.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A annotation to declare that extension of third-party functionality.
 *
 * @see Nullable
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER,
    ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThirdExtension {

  String value() default "";

}
