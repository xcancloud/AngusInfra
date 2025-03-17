package cloud.xcan.sdf.spec.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class to which this annotation is applied is not thread-safe. This annotation primarily
 * exists for clarifying the non-thread-safety of a class that might otherwise be assumed to be
 * thread-safe, despite the fact that it is a bad idea to assume a class is thread-safe without good
 * reason.
 * <p>
 * Based on code developed by Brian Goetz and Tim Peierls and concepts published in 'Java
 * Concurrency in Practice' by Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes
 * and Doug Lea.
 *
 * @see ThreadSafe
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) // The original version used RUNTIME
public @interface NotThreadSafe {

}
