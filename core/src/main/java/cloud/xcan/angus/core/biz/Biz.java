package cloud.xcan.angus.core.biz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Custom annotation that extends Spring's {@link Service} annotation to provide
 * business logic layer component marking. This annotation is specifically designed
 * to identify business service classes in the application architecture.
 * </p>
 * 
 * <p>
 * Key features:
 * - Semantic marking of business logic components
 * - Full compatibility with Spring's component scanning
 * - Inherits all functionality from {@link Service} annotation
 * - Enables clear separation of business logic from other service layers
 * </p>
 * 
 * <p>
 * Usage example:
 * <pre>
 * &#64;Biz("userBusinessService")
 * public class UserBusinessService {
 *     // Business logic implementation
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * This annotation is retained at runtime and can be used for reflection-based
 * operations, component scanning, and architectural documentation.
 * </p>
 * 
 * @see Service
 * @see org.springframework.stereotype.Component
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface Biz {

  /**
   * <p>
   * Specifies the name of the business service bean.
   * This value is used as the bean name in the Spring application context.
   * </p>
   * 
   * <p>
   * If not specified, the default bean naming strategy will be applied,
   * typically using the class name with the first letter in lowercase.
   * </p>
   *
   * @return the suggested component name, if any (or empty String otherwise)
   */
  @AliasFor(annotation = Service.class)
  String value() default "";
}

