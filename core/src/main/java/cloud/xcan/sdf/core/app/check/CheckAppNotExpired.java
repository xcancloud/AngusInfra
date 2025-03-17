package cloud.xcan.sdf.core.app.check;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check whether the application is opened and expired when cloud service.
 * <p>
 * Check whether the application license expires when privatizing.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckAppNotExpired {

  String appCode();
}
