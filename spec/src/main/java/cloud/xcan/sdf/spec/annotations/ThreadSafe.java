package cloud.xcan.sdf.spec.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class to which this annotation is applied is thread-safe.  This means that no sequences of
 * accesses (reads and writes to public fields, calls to public methods) may put the object into an
 * invalid state, regardless of the interleaving of those actions by the runtime, and without
 * requiring any additional synchronization or coordination on the part of the caller.
 * <p>
 * Based on code developed by Brian Goetz and Tim Peierls and concepts published in 'Java
 * Concurrency in Practice' by Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes
 * and Doug Lea.
 *
 * @see NotThreadSafe
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ThreadSafe {

}
