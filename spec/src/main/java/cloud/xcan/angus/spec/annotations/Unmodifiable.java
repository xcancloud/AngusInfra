package cloud.xcan.angus.spec.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marking function cannot be modified, or other components or functions of the system need to be
 * modified greatly.
 * <p>
 * For example: quota upper limit.
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Unmodifiable {

}
