package cloud.xcan.angus.persistence.jpa.annotation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Convenience annotation for Angus applications using JPA.
 * <p>
 * This is a composed annotation that combines {@link SpringBootApplication} and
 * automatically excludes Spring Boot's default {@link DataSourceAutoConfiguration}
 * to avoid conflicts with Angus's custom JPA configuration.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;AngusSpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * </pre>
 *
 * @author XiaoLong Liu
 * @since 3.0.0
 * @see SpringBootApplication
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public @interface AngusSpringBootApplication {

  /**
   * Alias for {@link SpringBootApplication#scanBasePackages()}.
   */
  @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackages")
  String[] scanBasePackages() default {};

  /**
   * Alias for {@link SpringBootApplication#scanBasePackageClasses()}.
   */
  @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackageClasses")
  Class<?>[] scanBasePackageClasses() default {};

  /**
   * Alias for {@link ComponentScan#basePackages()}.
   */
  @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
  String[] value() default {};
}
