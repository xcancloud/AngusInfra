package cloud.xcan.sdf.spec.thread;

import static cloud.xcan.sdf.spec.experimental.Assert.assertHasText;

/**
 * {@link ThreadLocal} subclass that exposes a specified name as {@link #toString()} result
 * (allowing for introspection).
 *
 * @param <T> the value type
 * @see NamedInheritableThreadLocal
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {

  private final String name;

  public NamedThreadLocal(String name) {
    assertHasText(name, "Name must not be empty");
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}