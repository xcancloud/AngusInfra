package cloud.xcan.angus.core.biz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check whether the quotas (users, exec, exec_debug - INFO,WARN,ERROR) when privatizing.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SneakyThrow0 {

  String level() default "INFO"/*INFO,WARN,ERROr*/;

}
