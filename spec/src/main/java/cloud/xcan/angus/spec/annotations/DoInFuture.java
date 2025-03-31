package cloud.xcan.angus.spec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Mark feature is not complete or needs to continue to be optimized, and can be done in the
 * future.
 * <p>
 * Please use TODO in comments if you need to do it currently.
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR,
    ElementType.METHOD})
public @interface DoInFuture {

  String value();
}
